/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.reference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.reference.config.ReferenceConfigProperties;

@ExtendWith(MockitoExtension.class)
class MinioStorageUtilServiceImplTest {

  private static final String RELATIVE_PATH = "/folder-id/file-id";
  private static final String PARTITION_ID = "osdu";
  private static final String STAGING_BUCKET = "staging-area";
  private static final String PESISTENT_BUCKET = "persistent-area";
  private static final String STAGING_LOCATION = "http://osdu/staging-area/folder-id/file-id";
  private static final String PERSISTENT_LOCATION = "http://osdu/persistent-area/folder-id/file-id";

  @InjectMocks
  MinioStorageUtilServiceImpl minioStorageUtilService;

  @Mock
  ReferenceConfigProperties properties;

  @Test
  void testGetStagingLocation_Success() {
    when(properties.getDataPartitionId()).thenReturn(PARTITION_ID);
    when(properties.getStagingArea()).thenReturn(STAGING_BUCKET);
    String stagingLocation = minioStorageUtilService.getStagingLocation(RELATIVE_PATH,
        PARTITION_ID);
    assertEquals(STAGING_LOCATION, stagingLocation);
  }

  @Test
  void testGetPersistentLocation_Success() {
    when(properties.getDataPartitionId()).thenReturn(PARTITION_ID);
    when(properties.getPersistentArea()).thenReturn(PESISTENT_BUCKET);
    String persistentLocation = minioStorageUtilService.getPersistentLocation(RELATIVE_PATH,
        PARTITION_ID);
    assertEquals(PERSISTENT_LOCATION, persistentLocation);
  }
}