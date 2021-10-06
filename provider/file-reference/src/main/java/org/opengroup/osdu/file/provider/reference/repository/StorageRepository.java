/*
 * Copyright 2021 Google LLC
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

package org.opengroup.osdu.file.provider.reference.repository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StorageRepository implements IStorageRepository {

  private final MinioRepository minioRepository;
  private final ExpiryTimeUtil expiryTimeUtil;

  // TODO: check new SignedUrlParameters() for NPE

  @Override
  public SignedObject getSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, Method.GET, new SignedUrlParameters());
  }

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, Method.PUT, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObjectBasedOnParams(
      String bucketName, String filePath, SignedUrlParameters signedUrlParameters) {
    return prepareSignedObject(bucketName, filePath, Method.GET, signedUrlParameters);
  }

  private SignedObject prepareSignedObject(
      String bucketName,
      String filepath,
      Method httpMethod,
      SignedUrlParameters signedUrlParameters) {
    ExpiryTimeUtil.RelativeTimeValue expiryTimeInTimeUnit =
        expiryTimeUtil.getExpiryTimeValueInTimeUnit(signedUrlParameters.getExpiryTime());

    GetPresignedObjectUrlArgs args =
        GetPresignedObjectUrlArgs.builder()
            .bucket(bucketName)
            .object(filepath)
            .expiry(
                Math.toIntExact(expiryTimeInTimeUnit.getValue()),
                expiryTimeInTimeUnit.getTimeUnit())
            .method(httpMethod)
            .build();

    String signedUrl = minioRepository.getSignedUrl(args);
    log.debug("Signed URL for created storage object: {}", signedUrl);

    return SignedObject.builder()
        .uri(getObjectUri(bucketName, filepath))
        .url(buildUrl(signedUrl))
        .build();
  }

  private URI getObjectUri(String bucketName, String filePath) {
    return URI.create(String.format("%s%s/%s", "https://", bucketName, filePath));
  }

  private URL buildUrl(String signedUrl) {
    try {
      return new URL(signedUrl);
    } catch (MalformedURLException e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Malformed signed url created.", e.getMessage());
    }
  }
}
