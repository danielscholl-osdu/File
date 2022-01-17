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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.filecollection.DatasetProperties;
import org.opengroup.osdu.file.model.filecollection.FileCollectionOperationResponse;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("FileCollectionDmsService")
@Data
@Slf4j
public class FileCollectionDmsServiceImpl implements IDmsService {

  @Autowired
  @Qualifier("FileCollectionStorageService")
  final IStorageService storageService;

  @Autowired
  final DpsHeaders headers;

  @Autowired
  final DataLakeStorageFactory storageFactory;

  @Autowired
  final ICloudStorageOperation cloudStorageOperation;

  @Autowired
  @Qualifier("FileCollectionUtilService")
  final IStorageUtilService storageUtilService;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public StorageInstructionsResponse getStorageInstructions() {
    String directoryID = generateDirectoryId();

    log.debug("Create the empty directory with DirectoryId : {}", directoryID);
    return storageService.createStorageInstructions(directoryID,
        headers.getPartitionIdWithFallbackToAccountId());
  }

  @Override
  public RetrievalInstructionsResponse getRetrievalInstructions(RetrievalInstructionsRequest retrievalInstructionsRequest) {
    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);

    try {
      MultiRecordInfo batchRecordsResponse = dataLakeStorage.getRecords(retrievalInstructionsRequest.getDatasetRegistryIds());
      List<Record> datasetMetadataRecords = batchRecordsResponse.getRecords();
      List<FileRetrievalData> fileRetrievalData = buildUnsignedUrls(datasetMetadataRecords);

      return this.storageService.createRetrievalInstructions(fileRetrievalData);
    } catch (StorageException storageExc) {
      final int statusCode = storageExc.getHttpResponse() != null ?
          storageExc.getHttpResponse().getResponseCode() : 500;
      log.error("Unable to fetch metadata for the datasets", storageExc);
      throw new AppException(statusCode, "Unable to fetch metadata for the datasets", storageExc.getMessage(), storageExc);

    }
  }

  private List<FileRetrievalData> buildUnsignedUrls(List<Record> datasetRegistryRecords) {
    List<FileRetrievalData> fileCollectionRetrievalDataList = new ArrayList<>();

    for(Record datasetRegistryRecord : datasetRegistryRecords){
      String fileCollectionPath = getFileCollectionPath(datasetRegistryRecord);

      String fileCollectionAbsolutePath = storageUtilService.getPersistentLocation(fileCollectionPath,
          headers.getPartitionId());

      FileRetrievalData fileCollectionRetrievalData = FileRetrievalData.builder()
          .recordId(datasetRegistryRecord.getId())
          .unsignedUrl(fileCollectionAbsolutePath).build();

      fileCollectionRetrievalDataList.add(fileCollectionRetrievalData);
    }
    return fileCollectionRetrievalDataList;
  }

  @Override
  public List<CopyDmsResponse> copyDatasetsToPersistentLocation(List<Record> datasetSources) {
    List<FileCopyOperation> copyOperations = new ArrayList<>();
    List<CopyDmsResponse> copyDmsResponseList = new ArrayList<>();

    for (Record datasetSource: datasetSources) {
      final String fileCollectionPath = this.getFileCollectionPath(datasetSource);
      String stagingLocation = storageUtilService.getStagingLocation(fileCollectionPath, headers.getPartitionId());
      String persistentLocation = storageUtilService.getPersistentLocation(fileCollectionPath, headers.getPartitionId());
      copyOperations.add(FileCopyOperation.builder().sourcePath(stagingLocation).destinationPath(persistentLocation).build());
    }

    List<FileCollectionOperationResponse> fileCollectionOperationResponses
        = cloudStorageOperation.copyDirectory(copyOperations);

    for (int i = 0; i< datasetSources.size(); i++) {
      copyDmsResponseList.add(CopyDmsResponse.builder()
          .success(fileCollectionOperationResponses.get(i).isSuccess())
          .datasetBlobStoragePath(this.getFileCollectionPath(datasetSources.get(i)))
          .build());
    }

    return copyDmsResponseList;
  }

  private String generateDirectoryId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   *  "data": {
   *             "DatasetProperties": {
   *                     "FileCollectionPath" : "/abc
   *                 }
   *             }
   */
  private String getFileCollectionPath(Record datasetRegistryRecord){

    if (!datasetRegistryRecord.getData().containsKey("DatasetProperties")) {
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad Request",
          "Dataset Metadata does not contain dataset properties");
    }

    DatasetProperties datasetProperties = OBJECT_MAPPER.convertValue(
        datasetRegistryRecord.getData().get("DatasetProperties"), DatasetProperties.class);

    String fileCollectionPath = datasetProperties.getFileCollectionPath();
    if (StringUtils.isEmpty(fileCollectionPath)) {
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Bad Request",
          "File Collection Path is missing in the record metadata");
    }

    return fileCollectionPath;
  }
}
