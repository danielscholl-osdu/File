/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
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

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.model.delivery.SignedUrl.SignedUrlBuilder;
import org.opengroup.osdu.file.provider.gcp.config.properties.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.AccessBoundaryRule;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.AvailabilityCondition;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.DownScopedCredentials;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.DownScopedCredentialsService;
import org.opengroup.osdu.file.provider.gcp.service.downscoped.DownScopedOptions;
import org.opengroup.osdu.file.provider.interfaces.delivery.IDeliveryStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryStorageServiceImpl implements IDeliveryStorageService {

  public static final String AVAILABILITY_CONDITION_EXPRESSION =
      "resource.name.startsWith('projects/_/buckets/<<bucket>>/objects/<<folder>>/') " +
          "|| api.getAttribute('storage.googleapis.com/objectListPrefix', '').startsWith('<<folder>>')";
  public static final String MALFORMED_URL = "Malformed URL";
  private static final String URI_EXCEPTION_REASON = "Exception creating signed url";
  private static final String INVALID_GS_PATH_REASON = "Unsigned url invalid, needs to be full GS path";
  private static final HttpMethod signedUrlMethod = HttpMethod.GET;

  private final Storage storage;
  private final InstantHelper instantHelper;
  private final DownScopedCredentialsService downscopedCredentialsService;
  private final GcpConfigurationProperties gcpConfigurationProperties;

  @Override
  public SignedUrl createSignedUrl(String unsignedUrl, String authorizationToken) {
    Instant now = instantHelper.getCurrentInstant();

    String[] gsPathParts = unsignedUrl.split("gs://");

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_GS_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_GS_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String filePath = String
        .join("/", Arrays.copyOfRange(gsObjectKeyParts, 1, gsObjectKeyParts.length));
    SignedUrlBuilder signedUrlBuilder = SignedUrl.builder().createdAt(now);

    BlobId blobId = BlobId.of(bucketName, filePath);
    Blob blob = storage.get(blobId);

    if (Objects.nonNull(blob)) {
      log.debug("resource is a blob. get SignedUrl");
      URL url = generateSignedGcURL(blobId);
      try {
        signedUrlBuilder.url(url).uri(new URI(url.toString())).createdAt(now);
      } catch (URISyntaxException e) {
        log.error("There was an error generating the URI.", e);
        throw new AppException(org.apache.http.HttpStatus.SC_BAD_REQUEST, MALFORMED_URL,
            URI_EXCEPTION_REASON, e);
      }
    } else {
      DownScopedCredentials downScopedCredentials = getDownScopedCredentials(bucketName, filePath);
      try {
        signedUrlBuilder
            .connectionString(downScopedCredentials.refreshAccessToken().getTokenValue());
      } catch (IOException e) {
        log.error("There was an error getting the DownScoped token.", e);
        throw new AppException(org.apache.http.HttpStatus.SC_BAD_REQUEST, MALFORMED_URL,
            URI_EXCEPTION_REASON, e);
      }
    }
    return signedUrlBuilder.build();
  }

  private DownScopedCredentials getDownScopedCredentials(String bucketName, String filePath) {
    log.debug("resource is not a blob. assume it is a folder. get DownScoped token");

    String availabilityConditionExpression = AVAILABILITY_CONDITION_EXPRESSION
        .replace("<<bucket>>", bucketName)
        .replace("<<folder>>", filePath);

    AvailabilityCondition ap = new AvailabilityCondition("obj", availabilityConditionExpression);

    AccessBoundaryRule abr = new AccessBoundaryRule(
        "//storage.googleapis.com/projects/_/buckets/" + bucketName,
        Collections.singletonList("inRole:roles/storage.objectViewer"),
        ap);

    DownScopedOptions downScopedOptions = new DownScopedOptions(Collections.singletonList(abr));
    return downscopedCredentialsService
        .getDownScopedCredentials(downScopedOptions);
  }

  private URL generateSignedGcURL(BlobId blobId) {

    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();

    return storage.signUrl(blobInfo,
        gcpConfigurationProperties.getSignedUrl().getExpirationDays(), TimeUnit.DAYS,
        SignUrlOption.httpMethod(signedUrlMethod),
        SignUrlOption.withV4Signature());
  }
}
