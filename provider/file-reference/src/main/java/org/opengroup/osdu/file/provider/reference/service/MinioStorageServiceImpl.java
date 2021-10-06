/*
 * Copyright 2021 Google LLC
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

package org.opengroup.osdu.file.provider.reference.service;

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
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.reference.config.FileLocationProperties;
import org.opengroup.osdu.file.provider.reference.config.ReferenceConfigProperties;
import org.opengroup.osdu.file.provider.reference.model.FileDmsDownloadLocation;
import org.opengroup.osdu.file.provider.reference.model.FileDmsUploadLocation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements IStorageService {

  private static final String MALFORMED_URL = "Malformed URL";
  private static final String INVALID_PATH_REASON = "Unsigned url invalid, needs to be full path";
  private static final String PROVIDER_KEY = "Anthos";

  private final FileLocationProperties fileLocationProperties;
  private final ReferenceConfigProperties referenceConfigProperties;
  private final IStorageRepository storageRepository;
  private final DpsHeaders dpsHeaders;
  private final ObjectMapper objectMapper;

  @Override
  public SignedUrl createSignedUrl(String fileName, String authorizationToken, String partitionID) {
    String bucketName = referenceConfigProperties.getStagingArea();
    String filepath = buildRelativePath(fileName);
    log.debug("Creating the signed blob for fileName: {}. Bucket: {}. Filepath: {}.",
        fileName, bucketName, filepath);
    return createSignedUrl(bucketName, filepath);
  }

  public SignedUrl createSignedUrl(String bucketName, String filepath) {
    checkFilepathLength(filepath);
    SignedObject signedObject = storageRepository.createSignedObject(bucketName, filepath);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource(buildRelativeFileSource(filepath))
        .createdBy(fileLocationProperties.getUserId())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  private void checkFilepathLength(String filepath) {
    if (filepath.length() > 1024) {
      throw new IllegalArgumentException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          1024, filepath.length()));
    }
  }

  @Override
  public StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionID) {
    SignedUrl signedUrl = this.createSignedUrl(datasetId, dpsHeaders.getAuthorization(), partitionID);

    FileDmsUploadLocation dmsLocation = FileDmsUploadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .createdBy(signedUrl.getCreatedBy())
        .fileSource(signedUrl.getFileSource())
        .build();

    Map<String, Object> uploadLocation =
        objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

    return StorageInstructionsResponse.builder()
        .providerKey(PROVIDER_KEY)
        .storageLocation(uploadLocation)
        .build();
  }

  @Override
  public SignedUrl createSignedUrlFileLocation(
      String unsignedUrl, String authorizationToken, SignedUrlParameters signedUrlParameters) {
    String[] gsObjectKeyParts = getObjectKeyParts(unsignedUrl);

    String bucketName = gsObjectKeyParts[1];
    String filePath =
        String.join("/", Arrays.copyOfRange(gsObjectKeyParts, 2, gsObjectKeyParts.length));

    SignedObject signedObject =
        storageRepository.getSignedObjectBasedOnParams(bucketName, filePath, signedUrlParameters);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .createdBy(fileLocationProperties.getUserId())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  public String[] getObjectKeyParts(String unsignedUrl) {
    String[] pathParts = unsignedUrl.split("http://");
    if (pathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL, INVALID_PATH_REASON);
    }

    String[] gsObjectKeyParts = pathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL, INVALID_PATH_REASON);
    }
    return gsObjectKeyParts;
  }

  @Override
  public RetrievalInstructionsResponse createRetrievalInstructions(
      List<FileRetrievalData> fileRetrievalDataList) {
    List<DatasetRetrievalProperties> datasetRetrievalPropertiesList = fileRetrievalDataList.stream()
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

  private String buildRelativePath(String filename) {
    String folderName = UUID.randomUUID().toString();
    return format("%s/%s", folderName, filename);
  }

  private DatasetRetrievalProperties buildDatasetRetrievalProperties(FileRetrievalData fileRetrievalData) {
    SignedUrl signedUrl = this.createSignedUrlFileLocation(fileRetrievalData.getUnsignedUrl(),
        dpsHeaders.getAuthorization(), new SignedUrlParameters());

    FileDmsDownloadLocation dmsLocation = FileDmsDownloadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .fileSource(signedUrl.getFileSource())
        .createdBy(signedUrl.getCreatedBy())
        .build();

    Map<String, Object> downloadLocation =
        objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

    return DatasetRetrievalProperties.builder()
        .retrievalProperties(downloadLocation)
        .datasetRegistryId(fileRetrievalData.getRecordId())
        .build();
  }
}