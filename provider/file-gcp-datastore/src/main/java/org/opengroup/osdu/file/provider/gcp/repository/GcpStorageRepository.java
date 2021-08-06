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

import java.net.URI;
import java.net.URL;

import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.SignUrlOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.lang.String.format;
import static org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant.GCS_PROTOCOL;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GcpStorageRepository implements IStorageRepository {

  final private Storage storage;

  @Autowired
  private final ExpiryTimeUtil expiryTimeUtil;

  @Override
  public SignedObject getSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, HttpMethod.GET, new SignedUrlParameters());
  }

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, HttpMethod.PUT, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObjectBasedOnParams(String bucketName, String filePath,
      SignedUrlParameters signedUrlParameters) {
    return prepareSignedObject(bucketName, filePath, HttpMethod.GET, signedUrlParameters);
  }

  private SignedObject prepareSignedObject(
      String bucketName, String filepath, HttpMethod httpMethod, SignedUrlParameters signedUrlParameters) {
    log.debug("Creating the signed blob in bucket {} for path {}", bucketName, filepath);

    BlobId blobId = BlobId.of(bucketName, filepath);
    BlobInfo blobInfo = BlobInfo
        .newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();

    ExpiryTimeUtil.RelativeTimeValue expiryTimeInTimeUnit = expiryTimeUtil
        .getExpiryTimeValueInTimeUnit(signedUrlParameters.getExpiryTime());

    URL signedUrl = storage
        .signUrl(blobInfo, expiryTimeInTimeUnit.getValue(), expiryTimeInTimeUnit.getTimeUnit(),
            SignUrlOption.withV4Signature(), SignUrlOption.httpMethod(httpMethod));

    log.debug("Signed URL for created storage object. Object ID : {} , Signed URL : {}",
              blobId,
              signedUrl);
    return SignedObject.builder().uri(getObjectUri(bucketName, filepath)).url(signedUrl).build();
  }

  private URI getObjectUri(String bucketName, String filePath) {
    return URI.create(format("%s%s/%s", GCS_PROTOCOL, bucketName, filePath));
  }

}
