/*
 * Copyright 2020 Google LLC
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

import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Storage;
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
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.gcp.model.GcpFileDmsDownloadLocation;
import org.opengroup.osdu.file.provider.gcp.model.GcpFileDmsUploadLocation;
import org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.gcp.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleCloudStorageServiceImpl implements IStorageService {

  private static final String INVALID_GS_PATH_REASON = "Unsigned url invalid, needs to be full GS path";
  private static final String PROVIDER_KEY = "GCP";

  final FileLocationProperties fileLocationProperties;
  final IStorageRepository storageRepository;
  final DpsHeaders dpsHeaders;
  final ObjectMapper objectMapper;
  final GoogleCloudStorageUtil googleCloudStorageUtil;
  final ITenantFactory tenantFactory;
  final Storage storage;

  @Override
  public SignedUrl createSignedUrl(String fileName, String authorizationToken, String partitionID) {
    log.debug("Creating the signed blob for fileName : {}. Authorization : {}, partitionID : {}",
        fileName, authorizationToken, partitionID);

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionID);
    Instant now = Instant.now(Clock.systemUTC());

    String filepath = buildRelativePath(fileName);
    String bucketName = googleCloudStorageUtil.getStagingBucket(tenantInfo.getProjectId());
    String userDesID = getUserDesID(authorizationToken);
    log.debug("Create storage object for fileName {} in bucket {} with filepath {}",
        fileName, bucketName, filepath);

    if (filepath.length() > StorageConstant.GCS_MAX_FILEPATH) {
      throw new IllegalArgumentException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          StorageConstant.GCS_MAX_FILEPATH, filepath.length()));
    }

    SignedObject signedObject = storageRepository.createSignedObject(bucketName, filepath);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource(buildRelativeFileSource(filepath))
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  @Override
  public StorageInstructionsResponse createStorageInstructions(
      String datasetId, String partitionID) {
    SignedUrl signedUrl = this.createSignedUrl(datasetId, dpsHeaders.getAuthorization(), partitionID);

    GcpFileDmsUploadLocation dmsLocation = GcpFileDmsUploadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .createdBy(signedUrl.getCreatedBy())
        .fileSource(signedUrl.getFileSource()).build();

    Map<String, Object> uploadLocation =
        objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

    return StorageInstructionsResponse.builder()
        .providerKey(PROVIDER_KEY)
        .storageLocation(uploadLocation).build();
  }

  @Override
  public SignedUrl createSignedUrlFileLocation(String unsignedUrl,
      String authorizationToken, SignedUrlParameters signedUrlParameters) {
    Instant now = Instant.now(Clock.systemUTC());

    String[] gsPathParts = unsignedUrl.split("gs://");

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL", INVALID_GS_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL", INVALID_GS_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String userDesID = getUserDesID(authorizationToken);
    String filePath = String.join("/", Arrays.copyOfRange(gsObjectKeyParts, 1, gsObjectKeyParts.length));

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
      List<FileRetrievalData> fileRetrievalDataList) {
    List<DatasetRetrievalProperties> datasetRetrievalPropertiesList  = fileRetrievalDataList.stream()
        .map(this::buildDatasetRetrievalProperties)
        .collect(Collectors.toList());

    return RetrievalInstructionsResponse.builder()
        .datasets(datasetRetrievalPropertiesList)
        .providerKey(PROVIDER_KEY)
        .build();
  }

  private String buildRelativeFileSource(String filepath) {
    return "/" + filepath;
  }

  private String getUserDesID(String authorizationToken) {
    return fileLocationProperties.getUserId();
  }

  private String buildRelativePath(String filename) {
    String folderName = UUID.randomUUID().toString();

    return format("%s/%s", folderName, filename);
  }

  private DatasetRetrievalProperties buildDatasetRetrievalProperties(FileRetrievalData fileRetrievalData) {
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
}
