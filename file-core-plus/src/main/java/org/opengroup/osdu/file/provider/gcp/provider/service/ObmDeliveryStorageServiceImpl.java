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

package org.opengroup.osdu.file.provider.gcp.provider.service;

import java.net.URL;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.ObmPathProvider;
import org.opengroup.osdu.core.obm.core.model.DirectoryInfo;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.provider.gcp.config.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.interfaces.delivery.IDeliveryStorageService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObmDeliveryStorageServiceImpl implements IDeliveryStorageService {

  private final DpsHeaders dpsHeaders;
  private final Driver obmDriver;
  private final GcpConfigurationProperties properties;
  private final ObmPathProvider pathProvider;

  @Override
  public SignedUrl createSignedUrl(String unsignedUrl, String authorizationToken) {
    String partitionId = dpsHeaders.getPartitionId();
    DirectoryInfo bucketInfo = pathProvider.extractBucketInfoFromUnsignedUrl(unsignedUrl, partitionId);
    int expirationDays = properties.getSignedUrl().getExpirationDays();

    URL signedUrlForDownload = obmDriver.getSignedUrlForDownload(bucketInfo.getBucketName(),
        ObmDestination.builder().partitionId(partitionId).build(), bucketInfo.getDirectoryId(), expirationDays, TimeUnit.DAYS);

    return SignedUrl.builder().createdAt(Instant.now()).url(signedUrlForDownload).build();
  }
}
