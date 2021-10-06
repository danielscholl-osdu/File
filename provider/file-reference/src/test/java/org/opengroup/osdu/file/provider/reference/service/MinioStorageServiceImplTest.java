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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.reference.config.FileLocationProperties;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceImplTest {

  private static final String BUCKET_NAME = "staging-area";
  private static final String FILE_PATH = "folder-id/file-id";
  private static final String FILE_SOURCE = "/folder-id/file-id";
  private static final String CREATED_BY = "user";
  private static final String DATASET_ID = "dataset-id";
  private static final String PARTITION_ID = "osdu";
  private static final String UNSIGNED_URL = "http://osdu/staging-area/folder-id/file-id";
  private static final String TOKEN = "token";
  private static final String PROVIDER_KEY = "Anthos";

  @InjectMocks
  @Spy
  MinioStorageServiceImpl minioStorageService;

  @Mock
  IStorageRepository storageRepository;
  @Mock
  SignedObject signedObject;
  @Mock
  URI uri;
  @Mock
  URL url;
  @Mock
  FileLocationProperties fileLocationProperties;
  @Mock
  SignedUrl signedUrl;
  @Mock
  DpsHeaders dpsHeaders;
  @Mock
  ObjectMapper objectMapper;

  @Test
  void testCreateSignedUrl_Success() {
    when(storageRepository.createSignedObject(any(), any())).thenReturn(signedObject);
    when(fileLocationProperties.getUserId()).thenReturn(CREATED_BY);
    when(signedObject.getUrl()).thenReturn(url);
    when(signedObject.getUri()).thenReturn(uri);

    SignedUrl signedUrl = minioStorageService.createSignedUrl(BUCKET_NAME, FILE_PATH);

    assertEquals(url, signedUrl.getUrl());
    assertEquals(uri, signedUrl.getUri());
    assertEquals(FILE_SOURCE, signedUrl.getFileSource());
    assertEquals(CREATED_BY, signedUrl.getCreatedBy());
  }

  @Test
  void testCreateStorageInstructions_Success() {
    Map<String, Object> uploadLocation = new HashMap<>();
    doReturn(signedUrl).when(minioStorageService).createSignedUrl(any(), any(), any());
    when(signedUrl.getUrl()).thenReturn(url);
    when(signedUrl.getCreatedBy()).thenReturn(CREATED_BY);
    when(signedUrl.getFileSource()).thenReturn(FILE_SOURCE);
    when(objectMapper.convertValue(any(), (TypeReference<Object>) any()))
        .thenReturn(uploadLocation);

    StorageInstructionsResponse response = minioStorageService.createStorageInstructions(
        DATASET_ID, PARTITION_ID);

    assertEquals(PROVIDER_KEY, response.getProviderKey());
    assertEquals(uploadLocation, response.getStorageLocation());
  }
}