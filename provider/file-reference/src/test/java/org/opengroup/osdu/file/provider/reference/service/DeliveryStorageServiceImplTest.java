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
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.model.delivery.SignedUrl;

@ExtendWith(MockitoExtension.class)
class DeliveryStorageServiceImplTest {

  private static final String TOKEN = "";
  private static final String UNSIGNED_URL = "http://osdu/staging-area/folder-id/file-id";
  private static final String CONNECTION_STRING = "http://staging-area/folder-id/file-id";

  @InjectMocks
  DeliveryStorageServiceImpl deliveryStorageService;

  @Mock
  MinioStorageServiceImpl minioStorageService;
  @Mock
  org.opengroup.osdu.file.model.SignedUrl signedUrl;
  @Mock
  URI uri;
  @Mock
  URL url;

  private String[] objectKeyParts;

  @BeforeEach
  void setUp() {
    objectKeyParts = UNSIGNED_URL.split("http://")[1].split("/");
  }

  @Test
  void testCreateSignedUrl_Success() {
    when(minioStorageService.getObjectKeyParts(UNSIGNED_URL)).thenReturn(objectKeyParts);
    when(minioStorageService.createSignedUrl(any(), any())).thenReturn(signedUrl);
    when(signedUrl.getUri()).thenReturn(uri);
    when(signedUrl.getUrl()).thenReturn(url);
    when(signedUrl.getConnectionString()).thenReturn(CONNECTION_STRING);
    SignedUrl signedUrl = deliveryStorageService.createSignedUrl(UNSIGNED_URL, TOKEN);
    assertEquals(uri, signedUrl.getUri());
    assertEquals(url, signedUrl.getUrl());
    assertEquals(CONNECTION_STRING, signedUrl.getConnectionString());
  }
}