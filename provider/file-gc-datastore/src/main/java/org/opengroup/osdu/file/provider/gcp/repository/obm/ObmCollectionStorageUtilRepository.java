/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.repository.obm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObmCollectionStorageUtilRepository {

  private final ObjectMapper objectMapper;
  private final DpsHeaders dpsHeaders;
  private final Driver obmDriver;
  private final ObmStorageUtil obmStorageUtil;
  private final ExpiryTimeUtil expiryTimeUtil;
  private final IStorageRepository fileStorageRepository;

  public ObmCollectionStorageUtilRepository(ObjectMapper objectMapper,
      DpsHeaders dpsHeaders, Driver obmDriver,
      ObmStorageUtil obmStorageUtil, ExpiryTimeUtil expiryTimeUtil,
      @Qualifier("ObmStorageRepository") IStorageRepository fileStorageRepository) {
    this.objectMapper = objectMapper;
    this.dpsHeaders = dpsHeaders;
    this.obmDriver = obmDriver;
    this.obmStorageUtil = obmStorageUtil;
    this.expiryTimeUtil = expiryTimeUtil;
    this.fileStorageRepository = fileStorageRepository;
  }

  public List<SignedUrl> getSignedUrlsForDirectory(String bucketName, String filepath,
      SignedUrlParameters parameters) {
    List<SignedUrl> directoryUrls = new ArrayList<>();

    Instant now = Instant.now(Clock.systemUTC());
    String userDesID = this.dpsHeaders.getUserEmail();

    Iterable<Blob> files = obmDriver.listBlobsByPrefix(bucketName,
        obmStorageUtil.getDestination(dpsHeaders.getPartitionId()), filepath);

    files.forEach(file -> {
      SignedObject signedObject = fileStorageRepository.getSignedObjectBasedOnParams(
          bucketName, file.getName(), parameters);

      directoryUrls.add(SignedUrl.builder()
          .url(signedObject.getUrl())
          .uri(signedObject.getUri())
          .fileSource(filepath)
          .createdBy(userDesID)
          .createdAt(now)
          .build());
    });

    return directoryUrls;
  }

  public Map<String, Object> createSigningOptions(TenantInfo tenant, String bucket,
      String filepath, SignedUrlParameters parameters) {
    ExpiryTimeUtil.RelativeTimeValue expiryTime = expiryTimeUtil
        .getExpiryTimeValueInTimeUnit(parameters.getExpiryTime());
    Map<String, String> signingOptions = obmDriver.getSigningOptions(
        bucket,
        filepath,
        obmStorageUtil.getDestination(tenant.getDataPartitionId()),
        expiryTime.getValue(),
        expiryTime.getTimeUnit()
    );

    return objectMapper.convertValue(signingOptions, new TypeReference<Map<String, Object>>() {
    });
  }
}
