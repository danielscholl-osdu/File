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



import java.net.MalformedURLException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.obm.core.EnvironmentResolver;
import org.opengroup.osdu.core.obm.core.UrlProvider;
import org.opengroup.osdu.core.obm.core.model.ObmHttpMethod;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.core.obm.core.UrlProvider.RESOURCE_ACCESS_STRING_FORMAT;

@Component("ObmCollectionStorageRepository")
@Slf4j
@RequiredArgsConstructor
public class ObmCollectionStorageRepository implements IStorageRepository {

  public static final String WARNING_MALFORMED_URL = "Signed URL was malformed";
  public static final String WARNING_INVALID_METHOD = "No valid HTTP method was passed. Got: %s";

  private final DpsHeaders dpsHeaders;
  private final EnvironmentResolver environmentResolver;
  private final UrlProvider urlProvider;

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.POST);
  }

  @Override
  public SignedObject getSignedObject(String bucketName, String filepath) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.GET);
  }

  @Override
  public SignedObject getSignedObjectBasedOnParams(String bucketName, String filepath, SignedUrlParameters signedUrlParameters) {
    return prepareSignedObject(bucketName, filepath, ObmHttpMethod.GET);
  }

  private SignedObject prepareSignedObject(String bucketName, String filepath, ObmHttpMethod httpMethod) {
    String partitionId = dpsHeaders.getPartitionId();

    try {
      return buildSignedObject(bucketName, filepath, partitionId, httpMethod);
    } catch (MalformedURLException e) {
      log.warn(WARNING_MALFORMED_URL, e);
      throw new AppException(
          HttpStatus.BAD_REQUEST.value(),
          HttpStatus.BAD_REQUEST.getReasonPhrase(),
          WARNING_MALFORMED_URL);
    }
  }

  private SignedObject buildSignedObject(String bucketName, String filepath,
      String partitionId, ObmHttpMethod httpMethod) throws MalformedURLException {

    SignedObject.SignedObjectBuilder builder = SignedObject.builder();

    if (ObmHttpMethod.POST.equals(httpMethod)) {
      builder.uri(getObjectUri(bucketName, partitionId));
      builder.url(urlProvider.getObjectUrl(bucketName, partitionId));
    } else if (ObmHttpMethod.GET.equals(httpMethod)) {
      builder.uri(getObjectUri(bucketName, filepath, partitionId));
      builder.url(urlProvider.getObjectUrl(bucketName, filepath, partitionId));
    } else {
      throw new AppException(
          HttpStatus.BAD_REQUEST.value(),
          HttpStatus.BAD_REQUEST.getReasonPhrase(),
          String.format(WARNING_INVALID_METHOD, httpMethod));
    }

    return builder.build();
  }

  private URI getObjectUri(String bucketName, String partitionId) {
    return getObjectUri(bucketName, "", partitionId);
  }

  private URI getObjectUri(String bucketName, String filepath, String partitionId) {
    return URI.create(
        String.format(RESOURCE_ACCESS_STRING_FORMAT,
            environmentResolver.getTransferProtocol(partitionId), bucketName, filepath));
  }
}
