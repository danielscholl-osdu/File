/*
 * Copyright 2021 Microsoft Corporation
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

package org.opengroup.osdu.file.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.dms.model.*;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.*;

@Service("FileDmsService")
@Slf4j
@RequiredArgsConstructor
public class FileDmsServiceImpl implements IDmsService {

  final IStorageService storageService;
  final DpsHeaders headers;
  final DataLakeStorageFactory storageFactory;
  final IStorageUtilService storageUtilService;
  final ICloudStorageOperation cloudStorageOperation;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public StorageInstructionsResponse getStorageInstructions() {
    String fileID = generateFileId();

    log.debug("Create the empty blob in bucket. FileID : {}", fileID);
    return storageService.createStorageInstructions(fileID,
        headers.getPartitionIdWithFallbackToAccountId());
  }

  private String generateFileId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Override
  public RetrievalInstructionsResponse getRetrievalInstructions(RetrievalInstructionsRequest retrievalInstructionsRequest, String expiryTime) {
    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);

    try {
      MultiRecordInfo batchRecordsResponse = dataLakeStorage.getRecords(retrievalInstructionsRequest.getDatasetRegistryIds());
      List<Record> datasetMetadataRecords = batchRecordsResponse.getRecords();
      List<FileRetrievalData> fileRetrievalData = buildUnsignedUrls(datasetMetadataRecords);

      return this.storageService.createRetrievalInstructions(fileRetrievalData, new SignedUrlParameters(expiryTime));

    } catch (StorageException storageExc) {
      final int statusCode = storageExc.getHttpResponse() != null ?
          storageExc.getHttpResponse().getResponseCode() : 500;
      log.error("Unable to fetch metadata for the datasets", storageExc);
      throw new AppException(statusCode, "Unable to fetch metadata for the datasets", storageExc.getMessage(), storageExc);

    }
  }

  @Override
  public RetrievalInstructionsResponse getRetrievalInstructions(RetrievalInstructionsRequest retrievalInstructionsRequest) {
      return getRetrievalInstructions(retrievalInstructionsRequest, null);
  }

  @Override
  public List<CopyDmsResponse> copyDatasetsToPersistentLocation(List<Record> datasetSources) {
    List<FileCopyOperation> copyOperations = new ArrayList<>();
    List<CopyDmsResponse> copyDmsResponseList = new ArrayList<>();

    for (Record datasetSource: datasetSources) {
      final String filePath = this.getStorageFilePath(datasetSource);
      String stagingLocation = storageUtilService.getStagingLocation(filePath, headers.getPartitionId());
      String persistentLocation = storageUtilService.getPersistentLocation(filePath, headers.getPartitionId());
      copyOperations.add(FileCopyOperation.builder().sourcePath(stagingLocation).destinationPath(persistentLocation).build());
    }

    List<FileCopyOperationResponse> copyResponses = cloudStorageOperation.copyFiles(copyOperations);

    for (int i = 0; i< datasetSources.size(); i++) {
      copyDmsResponseList.add(CopyDmsResponse.builder()
          .success(copyResponses.get(i).isSuccess())
          .datasetBlobStoragePath(copyResponses.get(i).getCopyOperation().getDestinationPath())
          .build());
    }

    return copyDmsResponseList;
  }

  private List<FileRetrievalData> buildUnsignedUrls(List<Record> datasetRegistryRecords){
    List<FileRetrievalData> fileRetrievalDataList = new ArrayList<>();
    for(Record datasetRegistryRecord : datasetRegistryRecords){
      String cloudStorageFilePath = getStorageFilePath(datasetRegistryRecord);

      //reject paths that are not files
      if (cloudStorageFilePath.trim().endsWith("/")) {
        throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid File Path",
            "Invalid File Path - Filename cannot contain trailing '/'");
      }

      String fileAbsolutePath = storageUtilService.getPersistentLocation(cloudStorageFilePath,
          headers.getPartitionId());

      FileRetrievalData fileRetrievalData = FileRetrievalData.builder()
          .recordId(datasetRegistryRecord.getId())
          .unsignedUrl(fileAbsolutePath).build();

      fileRetrievalDataList.add(fileRetrievalData);
    }
    return fileRetrievalDataList;
  }

  private String getStorageFilePath(Record datasetRegistryRecord){
    String storageFilePath;

    if (!datasetRegistryRecord.getData().containsKey("DatasetProperties")) {
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad Request",
          "Dataset Metadata does not contain dataset properties");
    }

    DatasetProperties datasetProperties = OBJECT_MAPPER.convertValue(
        datasetRegistryRecord.getData().get("DatasetProperties"), DatasetProperties.class);

    FileSourceInfo fileSourceInfo = datasetProperties.getFileSourceInfo();
    if (fileSourceInfo == null) {
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad Request",
          "File Source Info is missing in the record metadata");
    }

    if (!StringUtils.isEmpty(fileSourceInfo.getFileSource())) {
      storageFilePath = fileSourceInfo.getFileSource();
    } else if (!StringUtils.isEmpty(fileSourceInfo.getPreloadFilePath())) {
      storageFilePath = fileSourceInfo.getPreloadFilePath();
    } else  {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
          "No valid File Path found for File dataset",
          "Error finding unsigned path on record for signing");
    }

    return storageFilePath;
  }

  @Override
  public StorageInstructionsResponse getStorageInstructions(String expiryTime) {
    String fileID = generateFileId();

    log.debug("Create the empty blob in bucket. FileID : {}", fileID);
    return storageService.createStorageInstructions(fileID,
        headers.getPartitionIdWithFallbackToAccountId(), new SignedUrlParameters(expiryTime));
  }

}
