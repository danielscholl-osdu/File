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

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;
import org.opengroup.osdu.core.gcp.obm.driver.SignedDirectoryPropertiesResolver;
import org.opengroup.osdu.core.gcp.obm.persistence.ObmDestination;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.config.PartitionPropertyNames;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Primary
public class ObmCollectionStorageService implements IFileCollectionStorageService {

  private final ITenantFactory tenantFactory;
  private final DpsHeaders dpsHeaders;
  private final EnvironmentResolver environmentResolver;
  private final IStorageRepository collectionStorageRepository;
  private final PropertiesConfiguration properties;
  private final Driver obmDriver;
  private final ExpiryTimeUtil expiryTimeUtil;
  private final SignedDirectoryPropertiesResolver signedDirectoryPropertiesResolver;
  private final PartitionPropertyNames partitionPropertyNames;
  private final PartitionPropertyResolver partitionPropertyResolver;

  public ObmCollectionStorageService(
      ITenantFactory tenantFactory,
      DpsHeaders dpsHeaders,
      EnvironmentResolver environmentResolver,
      @Qualifier("ObmCollectionStorageRepository") IStorageRepository collectionStorageRepository,
      PropertiesConfiguration properties,
      Driver obmDriver,
      ExpiryTimeUtil expiryTimeUtil,
      SignedDirectoryPropertiesResolver signedDirectoryPropertiesResolver,
      PartitionPropertyNames partitionPropertyNames,
      PartitionPropertyResolver partitionPropertyResolver) {
    this.tenantFactory = tenantFactory;
    this.dpsHeaders = dpsHeaders;
    this.environmentResolver = environmentResolver;
    this.collectionStorageRepository = collectionStorageRepository;
    this.properties = properties;
    this.obmDriver = obmDriver;
    this.expiryTimeUtil = expiryTimeUtil;
    this.signedDirectoryPropertiesResolver = signedDirectoryPropertiesResolver;
    this.partitionPropertyNames = partitionPropertyNames;
    this.partitionPropertyResolver = partitionPropertyResolver;
  }

  /**
   * Create storage instructions for File collection Upload Operations in DMS API Context
   *
   * @param datasetId directory ID, a randomly generated UUID
   * @param partitionId partition ID
   * @return storage instructions: provider key and signed url for uploading file collection
   */
  @Override
  public StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionId) {
    String stagingBucket = partitionPropertyResolver.getOptionalPropertyValue(partitionPropertyNames.getStagingLocationName(), partitionId).orElseGet(() -> {
      TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
      return String.format("%s-%s-%s", tenantInfo.getProjectId(), tenantInfo.getName(), properties.getStagingArea());
    });

    log.info("Creating signed url for partition: {}, directoryId: {}. bucket: {}", partitionId, datasetId, stagingBucket);
    SignedUrl signedUrl = createSignedUrl(datasetId, stagingBucket);
    Map<String, String> signingOptions = createSigningOptions(datasetId, stagingBucket);

    Map<String, Object> uploadLocation = new LinkedHashMap<>();
    uploadLocation.put("url", signedUrl.getUrl().toString());
    uploadLocation.put("createdBy", signedUrl.getCreatedBy());
    uploadLocation.put("fileCollectionSource", signedUrl.getFileSource());
    uploadLocation.put("signingOptions", signingOptions);

    return StorageInstructionsResponse.builder()
        .providerKey(environmentResolver.getProviderKey())
        .storageLocation(uploadLocation).build();
  }

  /**
   * Create signed url for uploading file collection
   *
   * @param directoryId a randomly generated UUID
   * @param bucket staging bucket
   * @return signed url for uploading file collection
   */
  private SignedUrl createSignedUrl(String directoryId, String bucket) {
    SignedObject signedObject = collectionStorageRepository.createSignedObject(bucket, directoryId);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource(directoryId)
        .createdBy(dpsHeaders.getUserEmail())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  /**
   * Create signed options for uploading file collection
   * Provide access to specified directory
   *
   * @param datasetId a randomly generated UUID
   * @param stagingBucket staging bucket
   * @return signed options for uploading file collection
   */
  private Map<String, String> createSigningOptions(String datasetId, String stagingBucket) {
    ExpiryTimeUtil.RelativeTimeValue expiryTime = expiryTimeUtil.getExpiryTimeValueInTimeUnit(new SignedUrlParameters().getExpiryTime());

    return obmDriver.getSigningOptions(
        stagingBucket,
        datasetId + "/",
        ObmDestination.builder().partitionId(dpsHeaders.getPartitionId()).build(),
        expiryTime.getValue(),
        expiryTime.getTimeUnit()
    );
  }

  /**
   * Create retrieval instructions for File collection Download Operations in DMS API Context
   *
   * @param fileRetrievalData List of Unsigned URLs for which Signed URL / Temporary credentials should be generated.
   * @return retrieval instructions: provider key, datasetRegistryId, signed url or token for file collection
   */
  @Override
  public RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalData) {
    List<DatasetRetrievalProperties> datasetRetrievalProperties = fileRetrievalData.stream()
        .map(fileRetrievalDataItem -> DatasetRetrievalProperties.builder()
            .datasetRegistryId(fileRetrievalDataItem.getRecordId())
            .retrievalProperties(
                signedDirectoryPropertiesResolver.getSignedDirectoryProperties(fileRetrievalDataItem.getUnsignedUrl(),
                    dpsHeaders.getPartitionId()))
            .providerKey(environmentResolver.getProviderKey())
            .build())
        .collect(Collectors.toList());

    return RetrievalInstructionsResponse.builder()
        .datasets(datasetRetrievalProperties)
        .build();
  }
}
