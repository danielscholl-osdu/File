/*
 * Copyright 2020  Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.azure.service;

import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.CopyStatusType;
import org.elasticsearch.common.blobstore.BlobStoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import com.azure.storage.blob.models.BlobStorageException;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
public class CloudStorageOperationImplTest {

  @InjectMocks
  CloudStorageOperationImpl cloudStorageOperation;

  @Mock
  JaxRsDpsLog logger;

  @Mock
  BlobStore blobStore;

  @Mock
  BlobCopyInfo blobCopyInfo;

  @Mock
  DpsHeaders dpsHeaders;

  @Mock
  ServiceHelper serviceHelper;

  @BeforeEach
  public void init() {
    initMocks(this);
  }

  @Test
  public void copyFile_ShouldCall_BlobStoreCopyFileMethod() {
    // setup
    Mockito.when(blobStore.copyFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobCopyInfo);
    Mockito.when(blobCopyInfo.getCopyStatus()).thenReturn(CopyStatusType.SUCCESS);
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
          .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
          .thenReturn(TestUtils.RELATIVE_FILE_PATH);

    cloudStorageOperation.copyFile(TestUtils.ABSOLUTE_FILE_PATH,TestUtils.ABSOLUTE_FILE_PATH);
    verify(blobStore, times(1)).copyFile(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH,TestUtils.STAGING_CONTAINER_NAME,TestUtils.ABSOLUTE_FILE_PATH);
  }

  @Test
  public void copyFile_ShouldThrow_OsduBadRequestException_IfBlobStoreThrowsException() {
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    Mockito.doThrow(BlobStorageException.class).when(
        blobStore).copyFile(
            TestUtils.PARTITION,
            TestUtils.RELATIVE_FILE_PATH,
            TestUtils.STAGING_CONTAINER_NAME,
            TestUtils.ABSOLUTE_FILE_PATH);

    Assertions.assertThrows(OsduBadRequestException.class,()->{cloudStorageOperation.copyFile(TestUtils.ABSOLUTE_FILE_PATH,TestUtils.ABSOLUTE_FILE_PATH);});
  }

  @Test
  public void copyFile_ShouldThrow_OnReceivingBlankSourceOrDestinationFilePath() {
    String[] InvalidFilePaths = {"", "    ", null};
    for (String sourceFilePath : InvalidFilePaths) {
      // when
      Throwable thrown = catchThrowable(() -> cloudStorageOperation.copyFile(
          sourceFilePath, TestUtils.ABSOLUTE_FILE_PATH));
      // then
      then(thrown)
          .isInstanceOf(OsduBadRequestException.class)
          .hasMessageContaining(String.format("Illegal argument for source { %s } or destination { %s } file path",
              sourceFilePath, TestUtils.ABSOLUTE_FILE_PATH));

    }
    for (String destinationFilePath : InvalidFilePaths) {
      // when
      Throwable thrown = catchThrowable(() -> cloudStorageOperation.copyFile(
          TestUtils.ABSOLUTE_FILE_PATH, destinationFilePath));
      // then
      then(thrown)
          .isInstanceOf(OsduBadRequestException.class)
          .hasMessageContaining(String.format("Illegal argument for source { %s } or destination { %s } file path",
              TestUtils.ABSOLUTE_FILE_PATH, destinationFilePath));

    }
  }

  @Test
  public void deleteFile_ShouldCallDeleteFromStorageContainer() {
    // setup
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    // call
    cloudStorageOperation.deleteFile(TestUtils.ABSOLUTE_FILE_PATH);
    // verify
    verify(blobStore, times(1)).deleteFromStorageContainer(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH, TestUtils.STAGING_CONTAINER_NAME);
  }

  @Test
  public void deleteFile_ShouldThrow_OnReceivingBlankFilePath() {
    String[] InvalidFilePaths = {"", "    ", null};
    for(String filePath: InvalidFilePaths) {
      // when
      Throwable thrown = catchThrowable(()->cloudStorageOperation.deleteFile(
          filePath));
      // then
      then(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(String.format("invalid location received %s",filePath));
    }
  }
}
