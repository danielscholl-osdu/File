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

import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filecollection.DatasetCopyOperation;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import com.azure.storage.blob.models.BlobCopyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CloudStorageOperationImpl implements ICloudStorageOperation {
  @Autowired
  BlobStore blobStore;

  @Autowired
  DataLakeStore dataLakeStore;

  @Autowired
  JaxRsDpsLog logger;

  @Autowired
  DpsHeaders dpsHeaders;

  @Autowired
  ServiceHelper serviceHelper;

  private String loggerName = CloudStorageOperationImpl.class.getName();

  @Override
  public String copyFile(String sourceFilePath, String destinationFilePath) throws OsduBadRequestException {
    if(Strings.isBlank(sourceFilePath) || Strings.isBlank(destinationFilePath)) {
      throw new OsduBadRequestException(
          String.format("Illegal argument for source { %s } or destination { %s } file path",
              sourceFilePath,destinationFilePath));
    }

    String filePath = serviceHelper.getRelativeFilePathFromAbsoluteFilePath(destinationFilePath);
    String containerName = serviceHelper.getContainerNameFromAbsoluteFilePath(destinationFilePath);

    try {
      BlobCopyInfo copyInfo = blobStore.copyFile(dpsHeaders.getPartitionId(), filePath, containerName, sourceFilePath);
      logger.info(loggerName, copyInfo.getCopyStatus().toString());
      return copyInfo.getCopyId();
    }
    catch (BlobStorageException ex) {
      String message = FileMetadataConstant.INVALID_SOURCE_EXCEPTION + FileMetadataConstant.FORWARD_SLASH +  filePath;
      throw new OsduBadRequestException(message, ex);
    }
  }

  @Override
  public List<FileCopyOperationResponse> copyFiles(List<FileCopyOperation> fileCopyOperationList) {
    // TODO: Investigate if files can be copied in parallel with batch.
    List<FileCopyOperationResponse> operationResponses = new ArrayList<>();

    for (FileCopyOperation fileCopyOperation: fileCopyOperationList) {
      FileCopyOperationResponse response;
      try {
        String copyId = this.copyFile(fileCopyOperation.getSourcePath(),
            fileCopyOperation.getDestinationPath());
        response = FileCopyOperationResponse.builder()
            .copyOperation(fileCopyOperation)
            .success(true).build();
      } catch (Exception e) {
        logger.error("Error in performing file copy operation", e);
        response = FileCopyOperationResponse.builder()
            .copyOperation(fileCopyOperation)
            .success(false).build();
      }
      operationResponses.add(response);
    }
    return operationResponses;
  }

  @Override
  public Boolean deleteFile(String location) {
    if(Strings.isBlank(location)) {
      throw new IllegalArgumentException(String.format("invalid location received %s",location));
    }

    String filepath = serviceHelper.getRelativeFilePathFromAbsoluteFilePath(location);
    String containerName = serviceHelper.getContainerNameFromAbsoluteFilePath(location);
    return blobStore.deleteFromStorageContainer(dpsHeaders.getPartitionId(), filepath, containerName);
  }

  @Override
  public List<DatasetCopyOperation> copyDirectories(List<FileCopyOperation> fileCollectionPathList) {

    List<DatasetCopyOperation> operationResponses = new ArrayList<>();
    for (FileCopyOperation fileCopyOperation: fileCollectionPathList) {
      DatasetCopyOperation response;
      try {
        // moving file from staging to persistent location due to limitation of Azure DataLake
        this.move(dpsHeaders.getPartitionId(), fileCopyOperation.getSourcePath(),
            fileCopyOperation.getDestinationPath());
        response = DatasetCopyOperation.builder()
            .fileCopyOperation(fileCopyOperation)
            .success(true)
            .build();
      } catch (Exception e) {
        logger.error("Error in performing file copy operation", e);
        response = DatasetCopyOperation.builder()
            .fileCopyOperation(fileCopyOperation)
            .success(false)
            .build();
      }
      operationResponses.add(response);
    }
    return operationResponses;
  }

  //
  private void move(String partitionId, String sourcePath, String destinationPath) {

    if(StringUtils.isEmpty(sourcePath) || Strings.isBlank(destinationPath)) {
      throw new OsduBadRequestException(
          String.format("Illegal argument for source { %s } or destination { %s } file collection path",
              sourcePath, destinationPath));
    }
    String stagingFileSystem = serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(sourcePath);
    String persistentFileSystem = serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(destinationPath);
    String fileCollectionPath = serviceHelper.getRelativeDirectoryPathFromAbsoluteDirectoryPath(sourcePath);

    try {
      dataLakeStore.moveDirectory(partitionId, stagingFileSystem, fileCollectionPath, persistentFileSystem);
    } catch (DataLakeStorageException ex) {
      String message = FileMetadataConstant.INVALID_SOURCE_EXCEPTION + FileMetadataConstant.FORWARD_SLASH +  fileCollectionPath;
      throw new OsduBadRequestException(message, ex);
    }
  }
}
