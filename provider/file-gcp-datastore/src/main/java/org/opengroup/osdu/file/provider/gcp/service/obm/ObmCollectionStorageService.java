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

package org.opengroup.osdu.file.provider.gcp.service.obm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.model.GcpFileCollectionDmsUploadLocation;
import org.opengroup.osdu.file.provider.gcp.model.GcpFileDmsUploadLocation;
import org.opengroup.osdu.file.provider.gcp.repository.obm.ObmCollectionStorageUtilRepository;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Primary
public class ObmCollectionStorageService implements IFileCollectionStorageService {

  private static final String INVALID_GS_PATH_REASON =
      "Unsigned url invalid, needs to be full S3 storage path";

  private final ObmStorageUtil obmStorageUtil;
  private final ITenantFactory tenantFactory;
  private final DpsHeaders dpsHeaders;
  private final ObjectMapper objectMapper;
  private final EnvironmentResolver environmentResolver;
  private final ObmCollectionStorageUtilRepository collectionStorageUtilRepository;
  private final IStorageRepository collectionStorageRepository;

  public ObmCollectionStorageService(ObmStorageUtil obmStorageUtil,
      ITenantFactory tenantFactory, DpsHeaders dpsHeaders, ObjectMapper objectMapper,
      EnvironmentResolver environmentResolver,
      ObmCollectionStorageUtilRepository collectionStorageUtilRepository,
      @Qualifier("ObmCollectionStorageRepository") IStorageRepository collectionStorageRepository) {
    this.obmStorageUtil = obmStorageUtil;
    this.tenantFactory = tenantFactory;
    this.dpsHeaders = dpsHeaders;
    this.objectMapper = objectMapper;
    this.environmentResolver = environmentResolver;
    this.collectionStorageUtilRepository = collectionStorageUtilRepository;
    this.collectionStorageRepository = collectionStorageRepository;
  }

  @Override
  public StorageInstructionsResponse createStorageInstructions(String datasetId,
      String partitionID) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionID);
    String stagingBucket = obmStorageUtil.getStagingBucket(tenantInfo.getProjectId());
    SignedUrl signedUrl = this.createSignedUrl(datasetId, partitionID, stagingBucket);

    Map<String, Object> signingOptions = collectionStorageUtilRepository.createSigningOptions(
        tenantInfo, stagingBucket, datasetId + "/", new SignedUrlParameters());

    GcpFileCollectionDmsUploadLocation dmsLocation = GcpFileCollectionDmsUploadLocation.builder()
        .url(signedUrl.getUrl().toString())
        .signingOptions(signingOptions)
        .createdBy(signedUrl.getCreatedBy())
        .fileCollectionSource(signedUrl.getFileSource()).build();

    Map<String, Object> uploadLocation = objectMapper.convertValue(dmsLocation,
        new TypeReference<Map<String, Object>>() {});

    return StorageInstructionsResponse.builder()
        .providerKey(environmentResolver.getProviderKey())
        .storageLocation(uploadLocation).build();
  }

  @Override
  public RetrievalInstructionsResponse createRetrievalInstructions(
      List<FileRetrievalData> fileRetrievalData) {
    List<DatasetRetrievalProperties> datasetRetrievalProperties = new ArrayList<>(
        fileRetrievalData.size());

    for (FileRetrievalData fileRetrieval : fileRetrievalData) {
      List<SignedUrl> signedUrls = this.createSignedUrlFileLocations(fileRetrieval.getUnsignedUrl(),
          new SignedUrlParameters());

      List<Map<String, Object>> retrievalPropertiesList = new ArrayList<>();

      for (SignedUrl signedUrl : signedUrls) {
        GcpFileDmsUploadLocation dmsLocation = GcpFileDmsUploadLocation.builder()
            .signedUrl(signedUrl.getUrl().toString())
            .fileSource(signedUrl.getFileSource())
            .createdBy(signedUrl.getCreatedBy()).build();

        Map<String, Object> downloadLocation = objectMapper.convertValue(dmsLocation,
            new TypeReference<Map<String, Object>>() {});
        retrievalPropertiesList.add(downloadLocation);
      }

      Map<String, Object> retrievalProperties = new HashMap<String, Object>()
      {{
        put("retrievalPropertiesList", retrievalPropertiesList);
      }};

      DatasetRetrievalProperties datasetRetrievalProperty = DatasetRetrievalProperties.builder()
          .retrievalProperties(retrievalProperties)
          .datasetRegistryId(fileRetrieval.getRecordId())
          .build();

      datasetRetrievalProperties.add(datasetRetrievalProperty);
    }

    return RetrievalInstructionsResponse.builder()
        .datasets(datasetRetrievalProperties)
        .providerKey(environmentResolver.getProviderKey())
        .build();
  }

  private SignedUrl createSignedUrl(String directoryId, String partitionID, String bucket) {
    log.debug("Creating the signed directory for directoryId : {}. partitionID : {}",
        directoryId, partitionID);
    String userDesID = this.dpsHeaders.getUserEmail();
    log.debug("Create storage directory for directoryId {} in bucket {}",
        directoryId, bucket);

    SignedObject signedObject = collectionStorageRepository.createSignedObject(bucket, directoryId);
    Instant now = Instant.now(Clock.systemUTC());

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource(directoryId)
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  private List<SignedUrl> createSignedUrlFileLocations(String unsignedUrl,
      SignedUrlParameters signedUrlParameters) {

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String[] gsPathParts =
        unsignedUrl.split(environmentResolver.getTransferProtocol(tenantInfo.getDataPartitionId()));

    if (gsPathParts.length < 2) {
      throw new AppException(org.springframework.http.HttpStatus.BAD_REQUEST.value(), "Malformed URL",
          INVALID_GS_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(org.springframework.http.HttpStatus.BAD_REQUEST.value(), "Malformed URL",
          INVALID_GS_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String filePath =
        String.join("/", Arrays.copyOfRange(
            gsObjectKeyParts, 1, gsObjectKeyParts.length));

    return collectionStorageUtilRepository.getSignedUrlsForDirectory(
        bucketName, filePath, signedUrlParameters);
  }
}
