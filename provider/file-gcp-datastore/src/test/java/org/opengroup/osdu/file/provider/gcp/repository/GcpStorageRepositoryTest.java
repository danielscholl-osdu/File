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

package org.opengroup.osdu.file.provider.gcp.repository;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.extractor.Extractors.toStringMethod;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class GcpStorageRepositoryTest {

  private static final String BUCKET_NAME = "bucket";
  private static final String FILEPATH = "file/path/temp.tmp";

  @Captor
  private ArgumentCaptor<byte[]> contentCaptor;
  @Captor
  private ArgumentCaptor<SignUrlOption> optionsCaptor;

  @Test
  void shouldCreateSignedObject() {
    // given
    Storage storage = spyLocalStorage(TestCredential.getSa());
    IStorageRepository storageRepository = new GcpStorageRepository(storage);
    // when
    SignedObject signedObject = storageRepository.createSignedObject(BUCKET_NAME, FILEPATH);

    // then
    then(signedObject).isNotNull();

    verify(storage).signUrl(any(BlobInfo.class), eq(7L), eq(TimeUnit.DAYS),
        optionsCaptor.capture());

    then(optionsCaptor.getAllValues())
        .extracting("option", "value")
        .extracting(toStringMethod())
        .containsExactly("(SIGNATURE_VERSION, V4)","(HTTP_METHOD, PUT)");
  }

  @Test
  void shouldThrowExceptionWhenCallerIsNotSigner () {
    // given
    Storage storage = spyLocalStorage(TestCredential.getUserCredentials());
    IStorageRepository storageRepository = new GcpStorageRepository(storage);

    // when
    Throwable thrown = catchThrowable(() -> storageRepository.createSignedObject(BUCKET_NAME, FILEPATH));

    // then
    then(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Signing key was not provided and could not be derived");

    verify(storage).signUrl(any(BlobInfo.class), eq(7L), eq(TimeUnit.DAYS),
            optionsCaptor.capture());
  }

  private Storage spyLocalStorage(GoogleCredentials credentials) {
    Storage localStorage = LocalStorageHelper.getOptions().toBuilder()
        .setCredentials(credentials)
        .build()
        .getService();
    return mock(Storage.class, delegatesTo(localStorage));
  }
}
