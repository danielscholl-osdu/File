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
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filecollection.DatasetCopyOperation;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import com.azure.storage.blob.models.BlobStorageException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
public class CloudStorageOperationImplTest {

  private static final String FILE_COLLECTION_PATH = "fileCollectionPath";
  private static final String SOURCE_PATH = "sourcePath";
  private static final String DESTINATION_PATH = "destinationPath";

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

  @Mock
  DataLakeStore dataLakeStore;

  @BeforeEach
  public void init() {
    initMocks(this);
  }

  @Test
  public void copyFile_ShouldCall_BlobStoreCopyFileMethod() {
    // setup
    Mockito.when(blobStore.copyFile(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobCopyInfo);
    prepareMockCopyFile();
    cloudStorageOperation.copyFile(TestUtils.ABSOLUTE_FILE_PATH,TestUtils.ABSOLUTE_FILE_PATH);
    verify(blobStore, times(1)).copyFile(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH,TestUtils.STAGING_CONTAINER_NAME,TestUtils.ABSOLUTE_FILE_PATH);
    verifyMockCopyFile();
    verify(blobCopyInfo).getCopyStatus();
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

  @Test
  public void copyFiles_Success() {
    List<FileCopyOperation> fileCopyOperationList = getFileCopyOperationsForFile();

    List<FileCopyOperationResponse> responses = cloudStorageOperation.copyFiles(fileCopyOperationList);
    Assertions.assertTrue(responses.get(0).isSuccess());
    Assertions.assertEquals(fileCopyOperationList.get(0), responses.get(0).getCopyOperation());
  }

  @Test
  public void copyDirectories_Success() {
    prepareMockCopyDirectories();
    List<FileCopyOperation> fileCopyOperationList = getFileCopyOperations();
    when(dataLakeStore.moveDirectory(TestUtils.PARTITION, TestUtils.STAGING_CONTAINER_NAME,
        FILE_COLLECTION_PATH, TestUtils.PERSISTENT_CONTAINER_NAME)).thenReturn(mock(DataLakeDirectoryClient.class));

    List<DatasetCopyOperation> operationResponses = cloudStorageOperation.copyDirectories(fileCopyOperationList);

    DatasetCopyOperation operation = operationResponses.get(0);
    Assertions.assertTrue(operation.isSuccess());
    Assertions.assertEquals(operation.getFileCopyOperation(), fileCopyOperationList.get(0));
    verifyMockCopyDirectories();
  }

  @Test
  public void copyDirectories_EmptySourcePath() {
    List<FileCopyOperation> fileCopyOperationList = getFileCopyOperations_EmptySourcePath();

    List<DatasetCopyOperation> operationResponses = cloudStorageOperation.copyDirectories(fileCopyOperationList);

    DatasetCopyOperation operation = operationResponses.get(0);
    Assertions.assertFalse(operation.isSuccess());
    Assertions.assertEquals(operation.getFileCopyOperation(), fileCopyOperationList.get(0));
  }

  @Test
  public void copyDirectories_ThrowDataLakeException_WrongSourcePath() {
    prepareMockCopyDirectories();
    List<FileCopyOperation> fileCopyOperationList = getFileCopyOperations();
    DataLakeStorageException mockDataLakeStorageException = mock(DataLakeStorageException.class);
    when(mockDataLakeStorageException.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
    when(dataLakeStore.moveDirectory(TestUtils.PARTITION, TestUtils.STAGING_CONTAINER_NAME,
        FILE_COLLECTION_PATH, TestUtils.PERSISTENT_CONTAINER_NAME)).
        thenThrow(mockDataLakeStorageException);

    List<DatasetCopyOperation> operationResponses = cloudStorageOperation.copyDirectories(fileCopyOperationList);

    DatasetCopyOperation operation = operationResponses.get(0);
    Assertions.assertFalse(operation.isSuccess());
    Assertions.assertEquals(operation.getFileCopyOperation(), fileCopyOperationList.get(0));
    verifyMockCopyDirectories();
  }

  private void prepareMockCopyDirectories() {
    lenient().when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    lenient().when(serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(SOURCE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    lenient().when(serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(DESTINATION_PATH))
        .thenReturn(TestUtils.PERSISTENT_CONTAINER_NAME);
    lenient().when(serviceHelper.getRelativeDirectoryPathFromAbsoluteDirectoryPath(SOURCE_PATH))
        .thenReturn(FILE_COLLECTION_PATH);
  }

  private void verifyMockCopyDirectories() {
    verify(dpsHeaders).getPartitionId();
    verify(serviceHelper).getFileSystemNameFromAbsoluteDirectoryPath(SOURCE_PATH);
    verify(serviceHelper).getFileSystemNameFromAbsoluteDirectoryPath(DESTINATION_PATH);
    verify(serviceHelper).getRelativeDirectoryPathFromAbsoluteDirectoryPath(SOURCE_PATH);
  }

  private List<FileCopyOperation> getFileCopyOperations() {
    FileCopyOperation fileCopyOperation = new FileCopyOperation();
    fileCopyOperation.setSourcePath(SOURCE_PATH);
    fileCopyOperation.setDestinationPath(DESTINATION_PATH);

    List<FileCopyOperation> list = new ArrayList<>();
    list.add(fileCopyOperation);

    return list;
  }

  private List<FileCopyOperation> getFileCopyOperations_EmptySourcePath() {
    FileCopyOperation fileCopyOperation = new FileCopyOperation();
    fileCopyOperation.setDestinationPath(DESTINATION_PATH);

    List<FileCopyOperation> list = new ArrayList<>();
    list.add(fileCopyOperation);

    return list;
  }

  private List<FileCopyOperation> getFileCopyOperationsEmptySourcePath() {
    FileCopyOperation fileCopyOperation = new FileCopyOperation();
    fileCopyOperation.setDestinationPath(DESTINATION_PATH);

    List<FileCopyOperation> list = new ArrayList<>();
    list.add(fileCopyOperation);

    return list;
  }

  private List<FileCopyOperation> getFileCopyOperationsForFile() {
    FileCopyOperation fileCopyOperation = new FileCopyOperation();
    fileCopyOperation.setSourcePath(TestUtils.ABSOLUTE_FILE_PATH);
    fileCopyOperation.setDestinationPath(TestUtils.ABSOLUTE_FILE_PATH);

    List<FileCopyOperation> list = new ArrayList<>();
    list.add(fileCopyOperation);

    return list;
  }

  private void prepareMockCopyFile() {
    lenient().when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    lenient().when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    lenient().when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    lenient().when(blobCopyInfo.getCopyStatus()).thenReturn(CopyStatusType.SUCCESS);
  }

  private void verifyMockCopyFile() {
    verify(dpsHeaders).getPartitionId();
    verify(serviceHelper).getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH);
    verify(serviceHelper).getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH);
  }
}
