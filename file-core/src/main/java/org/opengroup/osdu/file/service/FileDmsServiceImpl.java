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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.file.model.DmsRecord;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service("FileDmsService")
@Slf4j
public class FileDmsServiceImpl implements IDmsService {
  @Inject
  private IStorageService storageService;

  @Inject
  private DpsHeaders headers;

  @Inject
  private DataLakeStorageFactory storageFactory;

  @Inject
  private IStorageUtilService storageUtilService;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public StorageInstructionsResponse getStorageInstructions() {
    String fileID = generateFileId();

    log.debug("Create the empty blob in bucket. FileID : {}", fileID);
    return storageService.createStorageInstructions(fileID, headers.getAuthorization(),
        headers.getPartitionIdWithFallbackToAccountId());
  }

  private String generateFileId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Override
  public RetrievalInstructionsResponse getRetrievalInstructions(RetrievalInstructionsRequest retrievalInstructionsRequest) {
    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);

    try {
      MultiRecordInfo batchRecordsResponse = dataLakeStorage.getRecords(retrievalInstructionsRequest.getDatasetRegistryIds());
      List<Record> datasetMetadataRecords = batchRecordsResponse.getRecords();
      List<DmsRecord> dmsRecords = createUnsignedUrls(datasetMetadataRecords);

      return this.storageService.createRetrievalInstructions(dmsRecords, headers.getAuthorization());

    } catch (StorageException storageExc) {
      final int statusCode = storageExc.getHttpResponse() != null ?
          storageExc.getHttpResponse().getResponseCode() : 500;
      log.error("Unable to fetch metadata for the datasets", storageExc);
      throw new AppException(statusCode, "Unable to fetch metadata for the datasets", storageExc.getMessage(), storageExc);

    }
  }

  private List<DmsRecord> createUnsignedUrls(List<Record> datasetRegistryRecords){
    List<DmsRecord> dmsRecords = new ArrayList<>();
    for(Record datasetRegistryRecord : datasetRegistryRecords){
      String cloudStorageFilePath = getStorageFilePath(datasetRegistryRecord);

      //reject paths that are not files
      if (cloudStorageFilePath.trim().endsWith("/")) {
        throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid File Path",
            "Invalid File Path - Filename cannot contain trailing '/'");
      }

      String fileAbsolutePath = storageUtilService.getPersistentLocation(cloudStorageFilePath,
          headers.getPartitionId());

      DmsRecord dmsRecord = DmsRecord.builder()
          .recordId(datasetRegistryRecord.getId())
          .unsignedUrl(fileAbsolutePath).build();

      dmsRecords.add(dmsRecord);
    }
    return dmsRecords;
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

    if (!StringUtils.isEmpty(fileSourceInfo.getFileSource())) {
      storageFilePath = fileSourceInfo.getFileSource();
    } else if (!StringUtils.isEmpty(fileSourceInfo.getPreLoadFilePath())) {
      storageFilePath = fileSourceInfo.getPreLoadFilePath();
    } else  {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
          "No valid File Path found for File dataset",
          "Error finding unsigned path on record for signing");
    }

    return storageFilePath;
  }
}
