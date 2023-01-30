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

package org.opengroup.osdu.file.provider.gcp.repository.obm;

import static java.lang.String.format;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.HttpMethod;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.model.ObmHttpMethod;
import org.opengroup.osdu.core.gcp.obm.model.ObmSignedUrlParams;
import org.opengroup.osdu.core.gcp.obm.model.ObmSignedUrlParams.ObmSignedUrlParamsBuilder;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ObmStorageRepository")
@RequiredArgsConstructor
public class ObmStorageRepository implements IStorageRepository {

  private static final String CONTENT_DISPOSITION_QUERY_PARAM = "response-content-disposition";
  private static final String CONTENT_TYPE_QUERY_PARAM = "response-content-type";
  public static final String ATTACHMENT_FILENAME = "attachment; filename=";

  private final DpsHeaders dpsHeaders;
  private final ITenantFactory tenantFactory;
  private final Driver obmStorageDriver;
  private final ObmStorageUtil obmStorageUtil;
  private final ExpiryTimeUtil expiryTimeUtil;
  private final EnvironmentResolver environmentResolver;

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, HttpMethod.PUT, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, HttpMethod.GET, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObjectBasedOnParams(String bucketName, String filepath,
      SignedUrlParameters signedUrlParameters) {
    return prepareSignedObject(bucketName, filepath, HttpMethod.GET, signedUrlParameters);
  }

  private SignedObject prepareSignedObject(
      String bucketName, String filepath, HttpMethod httpMethod,
      SignedUrlParameters signedUrlParameters) {
    log.debug("Creating the signed blob in bucket {} for path {}", bucketName, filepath);

    BlobId blobId = BlobId.of(bucketName, filepath);

    ExpiryTimeUtil.RelativeTimeValue expiryTimeInTimeUnit = expiryTimeUtil
        .getExpiryTimeValueInTimeUnit(signedUrlParameters.getExpiryTime());

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());

    ObmSignedUrlParamsBuilder obmSignedUrlParamsBuilder = ObmSignedUrlParams.builder()
        .method(ObmHttpMethod.valueOf(httpMethod.name()))
        .destination(obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()))
        .bucket(bucketName)
        .fileName(filepath)
        .expiryDuration(expiryTimeInTimeUnit.getValue())
        .expiryTimeUnit(expiryTimeInTimeUnit.getTimeUnit());

    String fileName = signedUrlParameters.getFileName();
    String contentType = signedUrlParameters.getContentType();

    if (Objects.nonNull(fileName) && !fileName.isEmpty()) {
      obmSignedUrlParamsBuilder.queryParams(
          Collections.singletonMap(CONTENT_DISPOSITION_QUERY_PARAM,
              ATTACHMENT_FILENAME + fileName));
    }

    if (Objects.nonNull(contentType) && !contentType.isEmpty()) {
      obmSignedUrlParamsBuilder.queryParams(
          Collections.singletonMap(CONTENT_TYPE_QUERY_PARAM, contentType));
    }

    ObmSignedUrlParams obmSignedUrlParams = obmSignedUrlParamsBuilder.build();

    URL signedUrl = obmStorageDriver.getSignedUrlWithParams(obmSignedUrlParams);

    log.debug("Signed URL for created storage object. Object ID : {} , Signed URL : {}",
        blobId,
        signedUrl);

    return SignedObject.builder()
        .uri(getObjectUri(bucketName, filepath, tenantInfo.getDataPartitionId())).url(signedUrl)
        .build();
  }

  private URI getObjectUri(String bucketName, String filePath, String partitionId) {
    return URI.create(
        format("%s%s/%s", environmentResolver.getTransferProtocol(partitionId), bucketName,
            filePath));
  }
}
