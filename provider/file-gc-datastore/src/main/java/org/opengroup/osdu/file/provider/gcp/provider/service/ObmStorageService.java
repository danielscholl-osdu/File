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

import static java.lang.String.format;
import static org.opengroup.osdu.file.provider.gcp.validation.GcFileLocationRequestValidator.GCS_MAX_FILEPATH;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;
import org.opengroup.osdu.core.gcp.obm.driver.ObmPathProvider;
import org.opengroup.osdu.core.gcp.obm.model.DirectoryInfo;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.config.PartitionPropertyNames;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ObmStorageService implements IStorageService {

  private final IStorageRepository storageRepository;
  private final ObmPathProvider pathProvider;
  private final ITenantFactory tenantFactory;
  private final DpsHeaders dpsHeaders;
  private final EnvironmentResolver environmentResolver;
  private final PropertiesConfiguration properties;
  private final PartitionPropertyNames partitionPropertyNames;
  private final PartitionPropertyResolver partitionPropertyResolver;

  @Autowired
  public ObmStorageService(@Qualifier("ObmStorageRepository") IStorageRepository storageRepository,
      ObmPathProvider pathProvider, ITenantFactory tenantFactory,
      DpsHeaders dpsHeaders, EnvironmentResolver environmentResolver, PropertiesConfiguration properties,
      PartitionPropertyNames partitionPropertyNames, PartitionPropertyResolver partitionPropertyResolver) {
    this.storageRepository = storageRepository;
    this.pathProvider = pathProvider;
    this.tenantFactory = tenantFactory;
    this.dpsHeaders = dpsHeaders;
    this.environmentResolver = environmentResolver;
    this.properties = properties;
    this.partitionPropertyNames = partitionPropertyNames;
    this.partitionPropertyResolver = partitionPropertyResolver;
  }

  @Override
  public SignedUrl createSignedUrl(String fileName, String authorizationToken, String partitionId) {
    log.debug("Creating the signed blob for fileName : {}. Authorization : {}, partitionId : {}",
        fileName, authorizationToken, partitionId);

    String filepath = generateRelativePath(fileName);

    String stagingBucket = partitionPropertyResolver.getOptionalPropertyValue(partitionPropertyNames.getStagingLocationName(), partitionId).orElseGet(() -> {
      TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
      return String.format("%s-%s-%s", tenantInfo.getProjectId(), tenantInfo.getName(), properties.getStagingArea());
    });
    log.debug("Create storage object for fileName {} in bucket {} with filepath {}",
        fileName, stagingBucket, filepath);

    SignedObject signedObject = storageRepository.createSignedObject(stagingBucket, filepath);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource("/" + filepath)
        .createdBy(dpsHeaders.getUserEmail())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  @Override
  public StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionID) {
    SignedUrl signedUrl = this.createSignedUrl(datasetId, dpsHeaders.getAuthorization(), partitionID);

    Map<String, Object> uploadLocation = new LinkedHashMap<>();
    uploadLocation.put("signedUrl", signedUrl.getUrl().toString());
    uploadLocation.put("createdBy", signedUrl.getCreatedBy());
    uploadLocation.put("fileSource", signedUrl.getFileSource());

    return StorageInstructionsResponse.builder()
        .providerKey(environmentResolver.getProviderKey())
        .storageLocation(uploadLocation).build();
  }

  @Override
  public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken, SignedUrlParameters signedUrlParameters) {
    DirectoryInfo bucketInfo = pathProvider.extractBucketInfoFromUnsignedUrl(unsignedUrl, dpsHeaders.getPartitionId());
    SignedObject signedObject = storageRepository.getSignedObjectBasedOnParams(bucketInfo.getBucketName(),
        bucketInfo.getDirectoryId(), signedUrlParameters);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .createdBy(this.dpsHeaders.getUserEmail())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  @Override
  public RetrievalInstructionsResponse createRetrievalInstructions(
      List<FileRetrievalData> fileRetrievalData) {
    List<DatasetRetrievalProperties> datasetRetrievalPropertiesList = fileRetrievalData.stream()
        .map(this::buildDatasetRetrievalProperties)
        .collect(Collectors.toList());

    return RetrievalInstructionsResponse.builder()
        .datasets(datasetRetrievalPropertiesList)
        .build();
  }

  private DatasetRetrievalProperties buildDatasetRetrievalProperties(FileRetrievalData fileRetrievalData) {
    SignedUrl signedUrl = this.createSignedUrlFileLocation(fileRetrievalData.getUnsignedUrl(),
        dpsHeaders.getAuthorization(), new SignedUrlParameters());

    Map<String, Object> downloadLocation = new LinkedHashMap<>();
    downloadLocation.put("signedUrl", signedUrl.getUrl().toString());
    downloadLocation.put("createdBy", signedUrl.getCreatedBy());
    downloadLocation.put("fileSource", signedUrl.getFileSource());

    return DatasetRetrievalProperties.builder()
        .retrievalProperties(downloadLocation)
        .datasetRegistryId(fileRetrievalData.getRecordId())
        .providerKey(environmentResolver.getProviderKey())
        .build();
  }

  private String generateRelativePath(String filename) {
    String folderName = UUID.randomUUID().toString();
    String filePath = format("%s/%s", folderName, filename);

    if (filePath.length() > GCS_MAX_FILEPATH) {
      throw new IllegalArgumentException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          GCS_MAX_FILEPATH, filePath.length()));
    }
    return filePath;
  }
}
