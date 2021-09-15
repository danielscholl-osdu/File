/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.provider.gcp.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.TestUtils;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class DataLakeStorageServiceImplTest {

  private static final String USER_DES_ID = "user des id";

  @InjectMocks
  GoogleCloudStorageServiceImpl storageService;

  @Mock
  IStorageRepository storageRepository;

  @Mock
  DpsHeaders dpsHeaders;

  @Captor
  ArgumentCaptor<String> filenameCaptor;

  @BeforeEach
  void setUp() {
    when(dpsHeaders.getUserEmail()).thenReturn(USER_DES_ID);
  }

  @Test
  void shouldCreateObjectSignedUrl_FileLocation() {
    // given
    SignedObject signedObject = getSignedObject();
    String unsignedUrl = TestUtils.getGcsObjectUri(TestUtils.BUCKET_NAME, "folder", "filename")
        .toString();

    given(storageRepository
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), anyString(), any()))
        .willReturn(signedObject);

    // when

    SignedUrl signedUrl = storageService
        .createSignedUrlFileLocation(unsignedUrl, TestUtils.AUTHORIZATION_TOKEN,
            new SignedUrlParameters());

    // then
    then(signedUrl).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.GCS_URL_CONDITION);
      then(url.getUri().toString()).matches(TestUtils.GCS_OBJECT_URI);
      then(url.getCreatedAt()).isBeforeOrEqualTo(now());

    });
    verify(storageRepository)
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), filenameCaptor.capture(), any());

  }

  @Test
  void shouldCreateObjectSignedUrl() {
    // given
    SignedObject signedObject = getSignedObject();
    String unsignedUrl = TestUtils.getGcsObjectUri(TestUtils.BUCKET_NAME, "folder", "filename")
        .toString();

    given(storageRepository
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), anyString(), any()))
        .willReturn(signedObject);


    SignedUrl signedUrl = storageService
        .createSignedUrlFileLocation(unsignedUrl, TestUtils.AUTHORIZATION_TOKEN,
            new SignedUrlParameters());

    // then
    then(signedUrl).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.GCS_URL_CONDITION);
      then(url.getUri().toString()).matches(TestUtils.GCS_OBJECT_URI);
      then(url.getCreatedAt()).isBeforeOrEqualTo(now());
    });
    verify(storageRepository)
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), filenameCaptor.capture(), any());

  }

  @Test
  void shouldCreateObjectSignedUrlBasedOnParamsProvided() {
    // given
    SignedObject signedObject = getSignedObject();
    String unsignedUrl = TestUtils.getGcsObjectUri(TestUtils.BUCKET_NAME, "folder", "filename")
        .toString();
    SignedUrlParameters parameters = new SignedUrlParameters("1H");

    given(storageRepository
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), anyString(), eq(parameters)))
        .willReturn(signedObject);

    // when
    SignedUrl signedUrl = storageService
        .createSignedUrlFileLocation(unsignedUrl, TestUtils.AUTHORIZATION_TOKEN,
            parameters);

    // then
    then(signedUrl).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.GCS_URL_CONDITION);
      then(url.getUri().toString()).matches(TestUtils.GCS_OBJECT_URI);
      then(url.getCreatedAt()).isBeforeOrEqualTo(now());

    });
    verify(storageRepository)
        .getSignedObjectBasedOnParams(eq(TestUtils.BUCKET_NAME), filenameCaptor.capture(),
            eq(parameters));

  }

  private SignedObject getSignedObject() {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = TestUtils.USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URI uri = TestUtils.getGcsObjectUri(bucketName, folderName, filename);
    URL url = TestUtils.getGcsObjectUrl(bucketName, folderName, filename);

    return SignedObject.builder()
        .uri(uri)
        .url(url)
        .build();
  }

  private Instant now() {
    return Instant.now(Clock.systemUTC());
  }
}
