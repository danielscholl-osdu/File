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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.util.FilePathUtil;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
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

  @BeforeEach
  void init() {
    initMocks(this);
    storageUtilService = new StorageUtilServiceImpl(blobStoreConfig, filePathUtil, blobServiceClientWrapper);
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
}
