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

package org.opengroup.osdu.file.provider.gcp.service.obm;

import static org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant.INVALID_S3_STORAGE_PATH_REASON;
import static org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant.MALFORMED_URL;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.model.delivery.SignedUrl.SignedUrlBuilder;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.config.properties.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.delivery.IDeliveryStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObmDeliveryStorageServiceImpl implements IDeliveryStorageService {

  private final DpsHeaders dpsHeaders;
  private final ITenantFactory tenantFactory;
  private final Driver obmStorageDriver;
  private final GcpConfigurationProperties gcpConfigurationProperties;
  private final ObmStorageUtil obmStorageUtil;
  private final EnvironmentResolver environmentResolver;


  @Override
  public SignedUrl createSignedUrl(String unsignedUrl, String authorizationToken) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String[] gsPathParts =
        unsignedUrl.split(environmentResolver.getTransferProtocol(tenantInfo.getDataPartitionId()));

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_S3_STORAGE_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_S3_STORAGE_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String filePath = String
        .join("/", Arrays.copyOfRange(gsObjectKeyParts, 1, gsObjectKeyParts.length));

    int expirationDays = gcpConfigurationProperties.getSignedUrl().getExpirationDays();
    URL signedUrlForDownload =
        obmStorageDriver.getSignedUrlForDownload(bucketName,
            obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()), filePath,
            expirationDays,
            TimeUnit.DAYS);

    SignedUrlBuilder signedUrlBuilder =
        SignedUrl.builder().createdAt(Instant.now()).url(signedUrlForDownload);

    return signedUrlBuilder.build();
  }
}
