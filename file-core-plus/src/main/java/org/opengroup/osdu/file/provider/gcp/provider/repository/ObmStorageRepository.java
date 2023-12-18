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

package org.opengroup.osdu.file.provider.gcp.provider.repository;

import static java.lang.String.format;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.EnvironmentResolver;
import org.opengroup.osdu.core.obm.core.model.ObmHttpMethod;
import org.opengroup.osdu.core.obm.core.model.ObmSignedUrlParams;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
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
  private final Driver obmDriver;
  private final ExpiryTimeUtil expiryTimeUtil;
  private final EnvironmentResolver environmentResolver;

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.PUT, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.GET, new SignedUrlParameters());
  }

  @Override
  public SignedObject getSignedObjectBasedOnParams(String bucketName, String filepath,
      SignedUrlParameters signedUrlParameters) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.GET, signedUrlParameters);
  }

  private SignedObject prepareSignedObject(
      String bucketName, String filepath, ObmHttpMethod httpMethod,
      SignedUrlParameters signedUrlParameters) {
    log.debug("Creating the signed blob in bucket {} for path {}", bucketName, filepath);
    String partitionId = dpsHeaders.getPartitionId();

    ExpiryTimeUtil.RelativeTimeValue expiryTimeInTimeUnit = expiryTimeUtil
        .getExpiryTimeValueInTimeUnit(signedUrlParameters.getExpiryTime());

    ObmSignedUrlParams.ObmSignedUrlParamsBuilder obmSignedUrlParamsBuilder = ObmSignedUrlParams.builder()
        .method(httpMethod)
        .destination(ObmDestination.builder().partitionId(partitionId).build())
        .bucket(bucketName)
        .fileName(filepath)
        .expiryDuration(expiryTimeInTimeUnit.getValue())
        .expiryTimeUnit(expiryTimeInTimeUnit.getTimeUnit());

    String fileName = signedUrlParameters.getFileName();
    String contentType = signedUrlParameters.getContentType();

    if (Objects.nonNull(fileName) && !fileName.isEmpty()) {
      String encompassFilename = encompassFilename(fileName);
      obmSignedUrlParamsBuilder.queryParams(
          Collections.singletonMap(CONTENT_DISPOSITION_QUERY_PARAM,
              ATTACHMENT_FILENAME + encompassFilename));
    }

    if (Objects.nonNull(contentType) && !contentType.isEmpty()) {
      obmSignedUrlParamsBuilder.queryParams(
          Collections.singletonMap(CONTENT_TYPE_QUERY_PARAM, contentType));
    }

    ObmSignedUrlParams obmSignedUrlParams = obmSignedUrlParamsBuilder.build();

    URL signedUrl = obmDriver.getSignedUrlWithParams(obmSignedUrlParams);

    log.debug("Signed URL for created storage object. BucketName: {}, FilePath: {}, Signed URL: {}",
        bucketName, filepath, signedUrl);

    return SignedObject.builder()
        .url(signedUrl)
        .uri(getObjectUri(bucketName, filepath, partitionId))
        .build();
  }

  private URI getObjectUri(String bucketName, String filePath, String partitionId) {
    String transferProtocol = environmentResolver.getTransferProtocol(partitionId);
    if (!transferProtocol.endsWith("/")) {
      transferProtocol = transferProtocol + "/";
    }
    return URI.create(format("%s%s/%s", transferProtocol, bucketName, filePath));
  }

  /**
   * Because of the varied approaches to URL-encoded entities in browsers,
   * we need to account for the file names that will be used later in the Blob Storage response's Content-Disposition header.
   * For instance, Firefox will replace '%2C' in 'testing%2Ccopy.txt' with its ASCII representation,
   * which is ','; however, Chrome does not, leading to the browser assigning the wrong name to the downloaded file.
   * @param filepath will be encompassed in quotes
   * @return "filepath"
   *
   */
  private String encompassFilename(String filepath) {
    return '"' + filepath + '"';
  }
}
