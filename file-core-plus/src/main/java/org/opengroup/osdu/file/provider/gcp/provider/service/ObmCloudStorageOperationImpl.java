/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.provider.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.EnvironmentResolver;
import org.opengroup.osdu.core.obm.core.ObmDriverRuntimeException;
import org.opengroup.osdu.core.obm.core.ObmPathProvider;
import org.opengroup.osdu.core.obm.core.model.Blob;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filecollection.DatasetCopyOperation;
/*import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;*/
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObmCloudStorageOperationImpl implements ICloudStorageOperation {

  private static final String INVALID_RESOURCE_PATH = "Storage record does not have a valid file url";
  private final ObmPathProvider pathProvider;
  private final Driver obmDriver;
  private final DpsHeaders dpsHeaders;
  private final EnvironmentResolver environmentResolver;

  @Override
  public String copyFile(String sourceFile, String destinationFile) throws OsduBadRequestException {
    String partitionId = dpsHeaders.getPartitionId();
    String fromBucket = pathProvider.extractBucketInfoFromUnsignedUrl(sourceFile, partitionId).getBucketName();
    String fromPath = pathProvider.getDirectoryPath(sourceFile, partitionId);
    String destinationBucket = pathProvider.extractBucketInfoFromUnsignedUrl(destinationFile, partitionId).getBucketName();
    String destinationPath = pathProvider.getDirectoryPath(destinationFile, partitionId);
    ObmDestination obmDestination = ObmDestination.builder().partitionId(partitionId).build();

    if (Stream.of(fromBucket, fromPath, destinationBucket, destinationPath).anyMatch(StringUtils::isEmpty)) {
      throwBadRequest(INVALID_RESOURCE_PATH);
    }

    Blob sourceBlob = obmDriver.getBlob(fromBucket, fromPath, obmDestination);
    if (sourceBlob == null) {
      throwBadRequest(getErrorMessageFileNotPresent(fromPath),
          FileMetadataConstant.INVALID_SOURCE_EXCEPTION + sourceFile);
    }

    String copyBlobPath = obmDriver.copyBlob(obmDestination, fromBucket, fromPath, destinationBucket, destinationPath);
    return environmentResolver.getTransferProtocol(partitionId) + copyBlobPath;
  }

  @Override
  public List<FileCopyOperationResponse> copyFiles(List<FileCopyOperation> fileCopyOperationList) {
    return fileCopyOperationList.stream().map(operation -> {
      try {
        this.copyFile(operation.getSourcePath(), operation.getDestinationPath());
        return FileCopyOperationResponse.builder().copyOperation(operation).success(Boolean.TRUE).build();
      } catch (OsduBadRequestException e) {
        log.error("Error in performing file copy operation", e);
        return FileCopyOperationResponse.builder().copyOperation(operation).success(Boolean.FALSE).build();
      }
    }).collect(Collectors.toList());
  }

  @Override
  public List<DatasetCopyOperation> copyDirectories(List<FileCopyOperation> fileCopyOperationList) {
    return fileCopyOperationList.stream().map(operation -> {
      try {
        this.copyDirectory(operation.getSourcePath(), operation.getDestinationPath());
        return DatasetCopyOperation.builder().fileCopyOperation(operation).success(Boolean.TRUE).build();
      } catch (OsduBadRequestException | ObmDriverRuntimeException e) {
        log.error("Error in performing file copy operation", e);
        return DatasetCopyOperation.builder().fileCopyOperation(operation).success(Boolean.FALSE).build();
      }
    }).collect(Collectors.toList());
  }

  private List<String> copyDirectory(String sourcePath, String destinationPath) throws OsduBadRequestException {
    String partitionId = dpsHeaders.getPartitionId();
    String fromBucket = pathProvider.extractBucketInfoFromUnsignedUrl(sourcePath, partitionId).getBucketName();
    String fromPath = pathProvider.getDirectoryPath(sourcePath, partitionId);
    String destinationBucket = pathProvider.extractBucketInfoFromUnsignedUrl(destinationPath, partitionId).getBucketName();
    String destinationFilePath = pathProvider.getDirectoryPath(destinationPath, partitionId);
    ObmDestination obmDestination = ObmDestination.builder().partitionId(partitionId).build();

    if (Stream.of(fromBucket, fromPath, destinationBucket, destinationFilePath).anyMatch(StringUtils::isEmpty)) {
      throwBadRequest(INVALID_RESOURCE_PATH);
    }

    Iterable<Blob> sourceBlobs = obmDriver.listBlobsByPrefix(fromBucket, obmDestination, fromPath);

    if (!sourceBlobs.iterator().hasNext()) {
      throwBadRequest(getErrorMessageFileNotPresent(fromPath),
          FileMetadataConstant.INVALID_SOURCE_EXCEPTION + sourcePath);
    }

    String transferProtocol = environmentResolver.getTransferProtocol(partitionId);

    return obmDriver.copyBlobs(obmDestination, fromBucket, fromPath, destinationBucket, destinationFilePath).stream()
        .map(filePath -> transferProtocol + filePath).collect(Collectors.toList());
  }

  @Override
  public Boolean deleteFile(String location) {
    String partitionId = dpsHeaders.getPartitionId();
    String bucketName = pathProvider.extractBucketInfoFromUnsignedUrl(location, partitionId).getBucketName();
    String filePath = location.split(bucketName)[1];

    return obmDriver.deleteBlob(bucketName, filePath, ObmDestination.builder().partitionId(partitionId).build());
  }

  private void throwBadRequest(String errorMessage, String errorMessageLog) throws OsduBadRequestException {
    OsduBadRequestException ex = new OsduBadRequestException(errorMessage);
    log.error(errorMessageLog != null ? errorMessageLog : errorMessage, ex);
    throw ex;
  }

  private void throwBadRequest(String errorMessage) throws OsduBadRequestException {
    throwBadRequest(errorMessage, null);
  }

  private String getErrorMessageFileNotPresent(String fromPath) {
    return FileMetadataConstant.INVALID_SOURCE_EXCEPTION + FileMetadataConstant.FORWARD_SLASH + fromPath;
  }
}
