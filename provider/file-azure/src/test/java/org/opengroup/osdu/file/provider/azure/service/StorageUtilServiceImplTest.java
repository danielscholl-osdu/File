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

import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.azure.util.FilePathUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class StorageUtilServiceImplTest {

  private StorageUtilServiceImpl storageUtilService;

  @Mock
  BlobStoreConfig blobStoreConfig;

  @Mock
  FilePathUtil filePathUtil;

  @Mock
  BlobServiceClientWrapper blobServiceClientWrapper;

  @Mock
  BlobStore blobStore;

  @Mock
  BlobProperties blobProperties;

  @Mock
  DpsHeaders dpsHeaders;

  @Mock
  ServiceHelper serviceHelper;

  @Mock
  JaxRsDpsLog log;

  @BeforeEach
  void init() {
    initMocks(this);
    storageUtilService = new StorageUtilServiceImpl(blobStoreConfig, filePathUtil, blobServiceClientWrapper, blobStore, serviceHelper, dpsHeaders, log);
  }

  @Test
  void getStagingLocation_ShouldReturnCorrectLocation() {
    // setup
    Mockito.when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    Mockito.when(blobStoreConfig.getStagingContainer()).thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(filePathUtil.normalizeFilePath(TestUtils.RELATIVE_FILE_PATH)).thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String expectedLocation = "https://" + TestUtils.STORAGE_NAME + ".blob.core.windows.net/"
        + TestUtils.STAGING_CONTAINER_NAME + "/" + TestUtils.RELATIVE_FILE_PATH;

    // method call
    String location = storageUtilService.getStagingLocation(TestUtils.RELATIVE_FILE_PATH,TestUtils.PARTITION);

    // verify
    Assertions.assertEquals(expectedLocation, location);
  }

  @Test
  void getPersistentLocation_ShouldReturnCorrectLocation() {
    //setup
    Mockito.when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    Mockito.when(blobStoreConfig.getPersistentContainer()).thenReturn(TestUtils.PERSISTENT_CONTAINER_NAME);
    Mockito.when(filePathUtil.normalizeFilePath(TestUtils.RELATIVE_FILE_PATH)).thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String expectedLocation = "https://" + TestUtils.STORAGE_NAME + ".blob.core.windows.net/"
        + TestUtils.PERSISTENT_CONTAINER_NAME + "/" + TestUtils.RELATIVE_FILE_PATH;

    // method call
    String location = storageUtilService.getPersistentLocation(TestUtils.RELATIVE_FILE_PATH,TestUtils.PARTITION);

    // verify
    Assertions.assertEquals(expectedLocation, location);
  }

  @Test
  public void getChecksum_ShouldCall_BlobStoreGetBlobPropertiesMethod() {
    // setup
    when(blobStore.readBlobProperties(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobProperties);
    when(blobProperties.getContentMd5()).thenReturn(TestUtils.BLOB_NAME.getBytes());
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+ StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);

    String checksum = storageUtilService.getChecksum(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);
    Assertions.assertNotNull(checksum);
    verify(blobStore, times(1)).readBlobProperties(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID,TestUtils.STAGING_CONTAINER_NAME);
  }

  @Test
  @DisplayName("getChecksum should skip checksum generation and return empty string")
  public void getChecksumShouldSkipHashGenerationAndReturnEmptyString() {
    //given
    when(blobStore.readBlobProperties(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobProperties);
    when(blobProperties.getBlobSize()).thenReturn(TestUtils.SIX_GB_BYTES);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    ReflectionTestUtils.setField(storageUtilService, TestUtils.BLOB_SIZE_LIMIT, TestUtils.BLOB_SIZE);
    when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);

    //when
    String checksum = storageUtilService.getChecksum(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);

    //then
    Assertions.assertNull(checksum);
    verify(blobStore, times(1)).readBlobProperties(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID,TestUtils.STAGING_CONTAINER_NAME);
  }

  @Test
  public void getChecksum_ShouldThrow_OsduBadRequestException_IfBlobStoreThrowsException() {
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);
    Mockito.doThrow(BlobStorageException.class).when(
        blobStore).readBlobProperties(
        TestUtils.PARTITION,
        TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID,
        TestUtils.STAGING_CONTAINER_NAME);

    Assertions.assertThrows(OsduBadRequestException.class,()->{storageUtilService.getChecksum(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);});
  }

  @Test
  public void getChecksum_ShouldCall_CalculateChecksumMethod() throws IOException {
    // setup
    when(blobStore.readBlobProperties(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobProperties);
    BlobInputStream blobInputStream = mock(BlobInputStream.class);
    when(blobStore.getBlobInputStream(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobInputStream);
    byte[] bytes = new byte[StorageConstant.AZURE_MAX_FILEPATH];
    ReflectionTestUtils.setField(storageUtilService, TestUtils.BLOB_SIZE_LIMIT, TestUtils.BLOB_SIZE);
    when(blobInputStream.read(bytes)).thenReturn(10).thenReturn(-1);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);

    String checksum = storageUtilService.getChecksum(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);
    Assertions.assertNotNull(checksum);
    verify(blobStore, times(1)).readBlobProperties(TestUtils.PARTITION, TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID,TestUtils.STAGING_CONTAINER_NAME);
  }

  @Test
  public void getChecksum_ShouldThrow_IOException_IfReadThrowsException() throws IOException {
    when(blobStore.readBlobProperties(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobProperties);
    BlobInputStream blobInputStream = mock(BlobInputStream.class);
    when(blobStore.getBlobInputStream(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(blobInputStream);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    ReflectionTestUtils.setField(storageUtilService, TestUtils.BLOB_SIZE_LIMIT, TestUtils.BLOB_SIZE);
    when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);
    byte[] bytes = new byte[StorageConstant.AZURE_MAX_FILEPATH];
    Mockito.doThrow(IOException.class).when(
        blobInputStream).read(bytes);

    Assertions.assertThrows(AppException.class,()->{storageUtilService.getChecksum(TestUtils.ABSOLUTE_FILE_PATH+StorageConstant.SLASH+TestUtils.FILE_ID);});
  }
}
