/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.util.obm;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;
import org.opengroup.osdu.core.gcp.obm.driver.ObmPathProvider;

@ExtendWith(MockitoExtension.class)
class ObmStoragePathProviderTest {

  private static final String BUCKET = "bucket";
  private static final String FULL_GS_PATH = "gs://bucket/directory/file.csv";
  private static final String FULL_MINIO_PATH = "https://minio.com/bucket/directory/file.csv";
  private static final String MINIO_PROTOCOL = "https://minio.com/";
  private static final String GS_PROTOCOL = "gs://";
  private static final String PARTITION_ID = "test";
  private static final String EXPECTED_DIR_PATH = "directory/file.csv";

  @Mock
  private EnvironmentResolver environmentResolver;
  private ObmPathProvider pathProvider;

  @BeforeEach
  void setUp() {
    pathProvider = new ObmPathProvider(environmentResolver);
  }

  @Test
  void testGetBucketNameFromFullGSPath() {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(GS_PROTOCOL);
    String bucketName = pathProvider.extractBucketInfoFromUnsignedUrl(FULL_GS_PATH, PARTITION_ID).getBucketName();
    assertEquals(BUCKET, bucketName);
  }

  @Test
  void testGetBucketNameFromFullMinioPath() {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_PROTOCOL);
    String bucketName = pathProvider.extractBucketInfoFromUnsignedUrl(FULL_MINIO_PATH, PARTITION_ID).getBucketName();
    assertEquals(BUCKET, bucketName);
  }

  @Test
  void testGetDirPathFromFullGSPath() {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(GS_PROTOCOL);
    String directoryPath = pathProvider.getDirectoryPath(FULL_GS_PATH, PARTITION_ID);
    assertEquals(EXPECTED_DIR_PATH, directoryPath);
  }

  @Test
  void testGetDirPathFromFullMinioPath() {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_PROTOCOL);
    String bucketName = pathProvider.getDirectoryPath(FULL_MINIO_PATH, PARTITION_ID);
    assertEquals(EXPECTED_DIR_PATH, bucketName);
  }
}
