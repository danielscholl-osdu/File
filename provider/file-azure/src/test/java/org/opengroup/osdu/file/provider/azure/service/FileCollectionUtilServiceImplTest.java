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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.DataLakeClientWrapper;
import org.opengroup.osdu.file.provider.azure.config.DataLakeConfig;
import org.opengroup.osdu.file.provider.azure.util.FilePathUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileCollectionUtilServiceImplTest {

  @Mock
  DataLakeConfig dataLakeConfig;

  @Mock
  DataLakeClientWrapper dataLakeClientWrapper;

  @Mock
  FilePathUtil filePathUtil;

  @InjectMocks
  FileCollectionUtilServiceImpl fileCollectionUtilService;

  @Test
  void shouldReturnCorrectLocation() {
    // setup
    when(dataLakeClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    when(dataLakeConfig.getStagingFileSystem()).thenReturn(TestUtils.STAGING_FILE_SYSTEM_NAME);
    when(filePathUtil.normalizeFilePath(TestUtils.RELATIVE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String expectedLocation = "https://" + TestUtils.STORAGE_NAME + ".dfs.core.windows.net/"
        + TestUtils.STAGING_FILE_SYSTEM_NAME + "/" + TestUtils.RELATIVE_FILE_PATH;

    // method call
    String location = fileCollectionUtilService.getStagingLocation(TestUtils.RELATIVE_FILE_PATH, TestUtils.PARTITION);

    // verify
    assertEquals(expectedLocation, location);
  }

  @Test
  void getPersistentLocation_ShouldReturnCorrectLocation() {
    //setup
    when(dataLakeClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    when(dataLakeConfig.getPersistentFileSystem()).thenReturn(TestUtils.PERSISTENT_FILE_SYSTEM_NAME);
    when(filePathUtil.normalizeFilePath(TestUtils.RELATIVE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String expectedLocation = "https://" + TestUtils.STORAGE_NAME + ".dfs.core.windows.net/"
        + TestUtils.PERSISTENT_FILE_SYSTEM_NAME + "/" + TestUtils.RELATIVE_FILE_PATH;

    // method call
    String location = fileCollectionUtilService.getPersistentLocation(TestUtils.RELATIVE_FILE_PATH,
        TestUtils.PARTITION);

    // verify
    assertEquals(expectedLocation, location);
  }
}
