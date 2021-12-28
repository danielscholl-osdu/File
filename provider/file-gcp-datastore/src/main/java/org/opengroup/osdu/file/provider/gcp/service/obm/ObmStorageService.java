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

import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.opengroup.osdu.file.provider.gcp.model.GcpFileDmsDownloadLocation;
import org.opengroup.osdu.file.provider.gcp.model.GcpFileDmsUploadLocation;
import org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class ObmStorageService implements IStorageService {

  private final IStorageRepository storageRepository;
  private final ObmStorageUtil obmStorageUtil;
  private final ITenantFactory tenantFactory;
  private final DpsHeaders dpsHeaders;
  private final ObjectMapper objectMapper;
  private final EnvironmentResolver environmentResolver;
  private static final String INVALID_GS_PATH_REASON =
      "Unsigned url invalid, needs to be full S3 storage path";

  @Override
  public SignedUrl createSignedUrl(String fileName, String authorizationToken, String partitionID) {
    log.debug("Creating the signed blob for fileName : {}. Authorization : {}, partitionID : {}",
        fileName, authorizationToken, partitionID);

    String filepath = buildRelativePath(fileName);

    if (filepath.length() > StorageConstant.GCS_MAX_FILEPATH) {
      throw new IllegalArgumentException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          StorageConstant.GCS_MAX_FILEPATH, filepath.length()));
    }

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionID);

    String stagingBucket = obmStorageUtil.getStagingBucket(tenantInfo.getProjectId());
    String userDesID = this.dpsHeaders.getUserEmail();
    log.debug("Create storage object for fileName {} in bucket {} with filepath {}",
        fileName, stagingBucket, filepath);

    SignedObject signedObject = storageRepository.createSignedObject(stagingBucket, filepath);
    Instant now = Instant.now(Clock.systemUTC());

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource("/" + filepath)
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  @Override
  public StorageInstructionsResponse createStorageInstructions(String datasetId,
      String partitionID) {

    SignedUrl signedUrl =
        this.createSignedUrl(datasetId, dpsHeaders.getAuthorization(), partitionID);

    GcpFileDmsUploadLocation dmsLocation = GcpFileDmsUploadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .createdBy(signedUrl.getCreatedBy())
        .fileSource(signedUrl.getFileSource()).build();

    Map<String, Object> uploadLocation =
        objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {
        });

    return StorageInstructionsResponse.builder()
        .providerKey(environmentResolver.getProviderKey())
        .storageLocation(uploadLocation).build();
  }

  @Override
  public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken,
      SignedUrlParameters signedUrlParameters) {
    Instant now = Instant.now(Clock.systemUTC());
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String[] gsPathParts =
        unsignedUrl.split(environmentResolver.getTransferProtocol(tenantInfo.getDataPartitionId()));

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL",
          INVALID_GS_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL",
          INVALID_GS_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String userDesID = this.dpsHeaders.getUserEmail();
    String filePath =
        String.join("/", Arrays.copyOfRange(gsObjectKeyParts, 1, gsObjectKeyParts.length));

    SignedObject signedObject = storageRepository.getSignedObjectBasedOnParams(bucketName, filePath,
        signedUrlParameters);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .createdBy(userDesID)
        .createdAt(now)
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
        .providerKey(environmentResolver.getProviderKey())
        .build();
  }

  private DatasetRetrievalProperties buildDatasetRetrievalProperties(
      FileRetrievalData fileRetrievalData) {
    SignedUrl signedUrl = this.createSignedUrlFileLocation(fileRetrievalData.getUnsignedUrl(),
        dpsHeaders.getAuthorization(), new SignedUrlParameters());

    GcpFileDmsDownloadLocation dmsLocation = GcpFileDmsDownloadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .fileSource(signedUrl.getFileSource())
        .createdBy(signedUrl.getCreatedBy()).build();

    Map<String, Object> downloadLocation = objectMapper.convertValue(dmsLocation,
        new TypeReference<Map<String, Object>>() {
        });

    return DatasetRetrievalProperties.builder()
        .retrievalProperties(downloadLocation)
        .datasetRegistryId(fileRetrievalData.getRecordId())
        .build();
  }

  private String buildRelativePath(String filename) {
    String folderName = UUID.randomUUID().toString();

    return format("%s/%s", folderName, filename);
  }
}
