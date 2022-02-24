/*
 * Copyright 2020  Microsoft Corporation
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

package org.opengroup.osdu.file.provider.azure.service;

import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.azure.config.DataLakeConfig;
import org.opengroup.osdu.file.provider.azure.model.AzureFileCollectionDmsUploadLocation;
import org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.azure.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Service
@Slf4j
@AllArgsConstructor
@Primary
public class FileCollectionStorageServiceImpl implements IFileCollectionStorageService {

  @Autowired
  DpsHeaders dpsHeaders;

  @Autowired
  DataLakeStore dataLakeStore;

  private static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String PROVIDER_KEY = "AZURE";

  @Autowired
  private final ExpiryTimeUtil expiryTimeUtil;

  @Autowired
  @Qualifier("DataLake")
  final IStorageRepository storageRepository;

  @Autowired
  final FileLocationProperties fileLocationProperties;

  @Autowired
  ServiceHelper serviceHelper;

  @Autowired
  DataLakeConfig dataLakeConfig;

  /**
   * Generates Signed URL for File Upload Operations in DMS API Context.
   *
   * @param directoryID  directoryID
   * @param partitionID partition ID
   * @return info about object URI, upload signed URL etc.
   */
  @Override
  public StorageInstructionsResponse createStorageInstructions(String directoryID, String partitionID) {
    SignedUrl signedUrl = this.createSignedUrl(directoryID, partitionID);

    AzureFileCollectionDmsUploadLocation dmsLocation = AzureFileCollectionDmsUploadLocation.builder()
        .signedUrl(signedUrl.getUrl().toString())
        .createdBy(signedUrl.getCreatedBy())
        .fileCollectionSource(signedUrl.getFileSource()).build();

    Map<String, Object> uploadLocation = OBJECT_MAPPER.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});
    StorageInstructionsResponse response = StorageInstructionsResponse.builder()
        .providerKey(PROVIDER_KEY)
        .storageLocation(uploadLocation).build();

    return response;
  }

  /**
   * Generates Signed URL for File Download Operations in DMS API Context.
   *
   * @param fileRetrievalDataList List of Unsigned URLs for which Signed URL / Temporary credentials should be generated.
   * @return info about object URI, download signed URL etc.
   */
  @Override
  public RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalDataList) {

    List<DatasetRetrievalProperties> datasetRetrievalProperties = new ArrayList<>(fileRetrievalDataList.size());

    for(FileRetrievalData fileRetrievalData : fileRetrievalDataList) {
      SignedUrl signedUrl = this.createSignedUrlDirectoryLocation(fileRetrievalData.getUnsignedUrl(),
           new SignedUrlParameters());

      AzureFileCollectionDmsUploadLocation dmsLocation = AzureFileCollectionDmsUploadLocation.builder()
          .signedUrl(signedUrl.getUrl().toString())
          .fileCollectionSource(signedUrl.getFileSource())
          .createdBy(signedUrl.getCreatedBy()).build();

      Map<String, Object> downloadLocation = OBJECT_MAPPER.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});
      DatasetRetrievalProperties datasetRetrievalProperty = DatasetRetrievalProperties.builder()
          .retrievalProperties(downloadLocation)
          .datasetRegistryId(fileRetrievalData.getRecordId())
          .build();

      datasetRetrievalProperties.add(datasetRetrievalProperty);
    }

    return RetrievalInstructionsResponse.builder()
        .datasets(datasetRetrievalProperties)
        .providerKey(PROVIDER_KEY)
        .build();
  }

  /**
   * Creates the empty object blob in storage.
   * Bucket name is determined by tenant using {@code partitionID}.
   * Object name is concat of a filepath and a fileID. Filepath is determined by user.
   *
   * @param directoryId        directory ID
   * @param partitionID        partition ID
   * @return info about object URI, signed URL and when and who created blob.
   */
  private SignedUrl createSignedUrl(String directoryId, String partitionID) {
    log.debug("Creating the signed url for directoryId : {}, partitionID : {}",
        directoryId, partitionID);
    Instant now = Instant.now(Clock.systemUTC());

    String containerName = dataLakeConfig.getStagingFileSystem();

    String userDesID = getUserDesID();
    String directoryName = getDirectoryName(now, directoryId, userDesID);

    if (directoryName.length() > StorageConstant.AZURE_MAX_FILEPATH) {
      throw new IllegalArgumentException(format(
          "The maximum directoryName length is %s characters, but got a name with %s characters",
          StorageConstant.AZURE_MAX_FILEPATH, directoryName.length()));
    }

    SignedObject signedObject = storageRepository.createSignedObject(containerName, directoryName);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .fileSource(getRelativeFileSource(directoryName))
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  private String getRelativeFileSource(String directoryName) {
    return "/" + directoryName;
  }

  /**
   * Gets a signed url from an unsigned url
   *
   * @param unsignedUrl
   * @param signedUrlParameters
   * @return
   */
  @SneakyThrows
  private SignedUrl createSignedUrlDirectoryLocation(String unsignedUrl,
                                                     SignedUrlParameters signedUrlParameters) {
    if (StringUtils.isBlank(unsignedUrl)) {
      throw new IllegalArgumentException(
          String.format("invalid received for authorizationToken (value: %s) or unsignedURL (value: %s)",
               unsignedUrl));
    }

    String fileSystemName = serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(unsignedUrl);
    String directoryPath = serviceHelper.getRelativeDirectoryPathFromAbsoluteDirectoryPath(unsignedUrl);

    FileSystemSasPermission permission = new FileSystemSasPermission();
    permission.setReadPermission(true);

    OffsetDateTime expiryTime = expiryTimeUtil
        .getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime());

    String signedUrlString = dataLakeStore
        .generatePreSignedURL(dpsHeaders.getPartitionId(), fileSystemName, directoryPath,
            expiryTime, permission);

    if(StringUtils.isBlank(signedUrlString)) {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error",
          String.format("Could not generate signed URL for directory location %s", unsignedUrl));
    }

    return SignedUrl.builder()
        .url(new URL(signedUrlString))
        .uri(URI.create(unsignedUrl))
        .createdBy(getUserDesID())
        .createdAt(Instant.now(Clock.systemUTC()))
        .build();
  }

  private String getUserDesID() {
    return fileLocationProperties.getUserId();
  }

  private String getDirectoryName(Instant instant, String directoryName, String userDesID) {
    String directoryNamePrefix = instant.toEpochMilli() + "-"
        + DATE_TIME_FORMATTER
        .withZone(ZoneOffset.UTC)
        .format(instant);

    return format("%s-%s-%s", userDesID, directoryNamePrefix, directoryName);
  }

}
