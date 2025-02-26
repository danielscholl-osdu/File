/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.service;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.KindValidationException;
import org.opengroup.osdu.file.exception.NotFoundException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.mapper.FileMetadataRecordMapper;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.FileMetadataResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.status.FileDatasetDetailsPublisher;
import org.opengroup.osdu.file.service.status.FileStatusPublisher;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.util.FileMetadataUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    final JaxRsDpsLog log;
    final DataLakeStorageFactory dataLakeStorageFactory;
    final IStorageUtilService storageUtilService;
    final ICloudStorageOperation cloudStorageOperation;
    final DpsHeaders dpsHeaders;
    final FileMetadataUtil fileMetadataUtil;
    final FileMetadataRecordMapper fileMetadataRecordMapper;
    final FileStatusPublisher fileStatusPublisher;
    final FileDatasetDetailsPublisher fileDatasetDetailsPublisher;

    public FileMetadataResponse saveMetadata(FileMetadata fileMetadata)
            throws OsduBadRequestException, StorageException, ApplicationException {

        log.info(FileMetadataConstant.METADATA_SAVE_STARTED);
        fileStatusPublisher.publishInProgressStatus();
        FileMetadataResponse fileMetadataResponse = new FileMetadataResponse();
        String stagingLocation = null;
        String persistentLocation = null;
        try {
            validateKind(fileMetadata.getKind());

            DataLakeStorageService dataLakeStorage = this.dataLakeStorageFactory.create(dpsHeaders);
            String filePath = fileMetadata.getData().getDatasetProperties().getFileSourceInfo().getFileSource();
            fileMetadata.setId(fileMetadataUtil.generateRecordId(dpsHeaders.getPartitionId(),
                    fetchEntityFromKind(fileMetadata.getKind())));

            stagingLocation = storageUtilService.getStagingLocation(filePath, dpsHeaders.getPartitionId());
            persistentLocation = storageUtilService.getPersistentLocation(filePath, dpsHeaders.getPartitionId());

            cloudStorageOperation.copyFile(stagingLocation, persistentLocation);
            String checksum = storageUtilService.getChecksum(stagingLocation);
            if (!StringUtils.isBlank(checksum)) {
              FileSourceInfo fileSourceInfo = fileMetadata.getData().getDatasetProperties().getFileSourceInfo();
              fileSourceInfo.setChecksum(checksum);
              fileSourceInfo.setChecksumAlgorithm(storageUtilService.getChecksumAlgorithm().toString());
            }
            Record fileMetadataRecord = fileMetadataRecordMapper.fileMetadataToRecord(fileMetadata);

            log.info("Save Record Id " + fileMetadataRecord.getId());
            UpsertRecords upsertRecords = dataLakeStorage.upsertRecord(fileMetadataRecord);
            log.info(upsertRecords.toString());
            fileMetadataResponse.setId(upsertRecords.getRecordIds().get(0));
            fileStatusPublisher.publishSuccessStatus(upsertRecords.getRecordIds().get(0),
                    upsertRecords.getRecordIdVersions().get(0));
            fileDatasetDetailsPublisher.publishDatasetDetails(upsertRecords.getRecordIds().get(0),
                    upsertRecords.getRecordIdVersions().get(0));

            /**
             * Issue: https://community.opengroup.org/osdu/platform/system/file/-/issues/76
             * Resolution:
             * 1. Check the staging file exists
             * 2. Catch deletion failures and ignore them as deletion failure should not
             *    invalidate the call to save metadata
             * 3. Delete should be the last step of metadata save process
             * */
            cleanupStagingLocation(stagingLocation, dataLakeStorage, fileMetadataRecord);
        } catch (StorageException e) {
            log.error("Error occurred while creating file metadata storage record " + e.getMessage(), e);
            cloudStorageOperation.deleteFile(persistentLocation);
            fileStatusPublisher.publishFailureStatus(e.getHttpResponse());
            throw e;
        } catch(OsduBadRequestException e) {
            log.error("Error occurred while creating file metadata storage record " + e.getMessage(), e);
            fileStatusPublisher.publishFailureStatus(e.getMessage(), HttpStatus.BAD_REQUEST.value());
            throw e;
        }catch (Exception e) {
            log.error("Error occurred while creating file metadata " + e.getMessage(), e);
            cloudStorageOperation.deleteFile(persistentLocation);
            fileStatusPublisher.publishFailureStatus(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw new ApplicationException("Error occurred while creating file metadata", e);
        }
        return fileMetadataResponse;
    }

    private void cleanupStagingLocation(String stagingLocation, DataLakeStorageService dataLakeStorage, Record fileMetadataRecord) {
      try{
        if(dataLakeStorage.getRecord(fileMetadataRecord.getId()) != null) {
          cloudStorageOperation.deleteFile(stagingLocation);
        }
      }
      catch (Exception e){
        log.warning("the file deletion failed for file id: " + fileMetadataRecord.getId());
      }
    }

    public RecordVersion getMetadataById(String id)
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {
        DataLakeStorageService dataLakeStorage = this.dataLakeStorageFactory.create(dpsHeaders);
        Record rec = null;
        log.info("Fetching Record Id");
        try {
            rec = dataLakeStorage.getRecord(id);

        } catch (StorageException storageExc) {
            log.error("Error occurred while fetching metadata from storage");

            HttpResponse response = storageExc.getHttpResponse();
            if (FileMetadataConstant.HTTP_CODE_400 == response.getResponseCode()) {
                log.error("Invalid file id", storageExc);
            } else {
                log.error("Failed to find record for the given file id.");
            }
            throw storageExc;
        }

        if (null == rec) {
            log.warning("Record Not Found");
            throw new NotFoundException("Record Not Found");
        }

        return fileMetadataRecordMapper.recordToRecordVersion(rec);
    }

    private void validateKind(String kind) {
        String[] kindArr = kind.split(FileMetadataConstant.KIND_SEPRATOR);

        if (kindArr.length != 4) {
            throw new KindValidationException("Invalid kind");
        } else if (!kindArr[1].equalsIgnoreCase(FileMetadataConstant.FILE_KIND_SOURCE)) {
            throw new KindValidationException("Invalid source in kind");
        } else if (!kindArr[2].equalsIgnoreCase(FileMetadataConstant.FILE_KIND_ENTITY)) {
            throw new KindValidationException("Invalid entity in kind");
        }
    }

    private String fetchEntityFromKind(String kind) {
        String[] kindArr = kind.split(FileMetadataConstant.KIND_SEPRATOR);

        return kindArr[2];
    }

    public void deleteMetadataRecord(String recordId)
            throws OsduBadRequestException, StorageException, NotFoundException, ApplicationException {
        log.info(FileMetadataConstant.METADATA_DELETE_STARTED);
        RecordVersion metaRecord = this.getMetadataById(recordId);
        deleteMetadataRecordFromStorage(recordId);
        deleteFileFromPersistentLocation(metaRecord);
    }

    private void deleteFileFromPersistentLocation(RecordVersion metaRecord) {
        String filePath = metaRecord.getData().getDatasetProperties().getFileSourceInfo().getFileSource();
        String persistentLocation = storageUtilService.getPersistentLocation(filePath, dpsHeaders.getPartitionId());
        boolean result = cloudStorageOperation.deleteFile(persistentLocation);
        log.info("Result of delete file from persistent location: " + result);
    }

    private void deleteMetadataRecordFromStorage(String recordId) throws StorageException {
        DataLakeStorageService dataLakeStorage = this.dataLakeStorageFactory.create(dpsHeaders);
        HttpResponse response = dataLakeStorage.deleteRecord(recordId);
        log.info("Http response code of deleting metadata from storage: " + response.getResponseCode());
        if (FileMetadataConstant.HTTP_CODE_204 != response.getResponseCode()) {
            log.error("Unable to delete metadata record from storage" + response.getBody());
            throw new StorageException(
                    "Unable to delete metadata record from storage. Check the inner HttpResponse for more info.",
                    response);
        }
    }

}
