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

package org.opengroup.osdu.file.provider.azure.repository;

import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.azure.TestUtils;

import java.net.URL;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DataLakeRepositoryTest {

  @Mock
  DataLakeStore dataLakeStore;

  @Mock
  DpsHeaders dpsHeaders;

  @InjectMocks
  DataLakeRepository dataLakeRepository;

  @Test
  public void shouldCreateSignedObject() {
    doNothing().when(dataLakeStore).createDirectory(TestUtils.PARTITION, TestUtils.STAGING_FILE_SYSTEM_NAME,
        TestUtils.DIRECTORY_NAME);

    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(dataLakeStore.generatePreSignedURL(eq(TestUtils.PARTITION), eq(TestUtils.STAGING_FILE_SYSTEM_NAME),
        eq(TestUtils.DIRECTORY_NAME) , any(OffsetDateTime.class), any(FileSystemSasPermission.class)))
        .thenReturn(getSignedUrl().toString());

    SignedObject signedObject = dataLakeRepository.createSignedObject
        (TestUtils.STAGING_FILE_SYSTEM_NAME, TestUtils.DIRECTORY_NAME);

    assertEquals(signedObject.getUrl().toString(), getSignedUrl().toString());
  }

  public URL getSignedUrl() {
    return TestUtils.getAzureObjectUrl(TestUtils.STAGING_FILE_SYSTEM_NAME, TestUtils.DIRECTORY_NAME);
  }
}
