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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObmCloudStorageOperationImpl implements ICloudStorageOperation {

  private static final String INVALID_RESOURCE_PATH =
      "Storage record does not have a valid file url";
  private final ObmStorageUtil obmStorageUtil;
  private final ITenantFactory tenantFactory;
  private final Driver obmStorageDriver;
  private final DpsHeaders dpsHeaders;
  private final EnvironmentResolver environmentResolver;

  @Override
  public String copyFile(String sourceFile, String destinationFile)
      throws OsduBadRequestException {

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String fromBucket = obmStorageUtil.getBucketName(sourceFile, tenantInfo);
    String fromPath = obmStorageUtil.getDirectoryPath(sourceFile, tenantInfo);
    String destinationBucket = obmStorageUtil.getBucketName(destinationFile, tenantInfo);
    String destinationFilePath = obmStorageUtil.getDirectoryPath(destinationFile, tenantInfo);

    if (Stream.of(fromBucket, fromPath, destinationBucket, destinationFilePath)
        .anyMatch(StringUtils::isEmpty)) {
      throwBadRequest(INVALID_RESOURCE_PATH);
    }

    Blob sourceBlob = obmStorageDriver.getBlob(fromBucket, fromPath,
        obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()));
    if (sourceBlob == null) {
      throwBadRequest(getErrorMessageFileNotPresent(fromPath),
          FileMetadataConstant.INVALID_SOURCE_EXCEPTION + sourceFile);
    }

    String copyBlobPath =
        obmStorageDriver.copyBlob(obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()),
            fromBucket, fromPath, destinationBucket,
            destinationFilePath);

    String transferProtocol =
        environmentResolver.getTransferProtocol(tenantInfo.getDataPartitionId());

    return transferProtocol + copyBlobPath;
  }

  @Override
  public List<FileCopyOperationResponse> copyFiles(
      List<FileCopyOperation> fileCopyOperationList) {
    return fileCopyOperationList.stream()
        .map(operation -> {
          try {
            this.copyFile(operation.getSourcePath(), operation.getDestinationPath());
            return FileCopyOperationResponse.builder()
                .copyOperation(operation)
                .success(Boolean.TRUE)
                .build();
          } catch (OsduBadRequestException e) {
            log.error("Error in performing file copy operation", e);
            return FileCopyOperationResponse.builder()
                .copyOperation(operation)
                .success(Boolean.FALSE)
                .build();
          }
        }).collect(Collectors.toList());
  }

  @Override
  public Boolean deleteFile(String location) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String bucketName = obmStorageUtil.getBucketName(location, tenantInfo);
    String filePath = location.split(bucketName)[1];

    return obmStorageDriver.deleteBlob(bucketName, filePath,
        obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()));
  }

  void throwBadRequest(String errorMessage, String errorMessageLog) throws OsduBadRequestException {
    OsduBadRequestException ex = new OsduBadRequestException(errorMessage);
    log.error(errorMessageLog != null ? errorMessageLog : errorMessage, ex);
    throw ex;
  }

  void throwBadRequest(String errorMessage) throws OsduBadRequestException {
    throwBadRequest(errorMessage, null);
  }

  private String getErrorMessageFileNotPresent(String fromPath) {
    return FileMetadataConstant.INVALID_SOURCE_EXCEPTION + FileMetadataConstant.FORWARD_SLASH
        + fromPath;
  }
}
