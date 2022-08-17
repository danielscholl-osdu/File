/*
 * Copyright 2021-2022 Google LLC
 * Copyright 2021-2022 EPAM Systems, Inc
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

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.file.constant.ChecksumAlgorithm;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObmCloudStorageUtilServiceImpl implements IStorageUtilService {

  private final ITenantFactory tenantFactory;
  private final ObmStorageUtil obmStorageUtil;
  private final EnvironmentResolver environmentResolver;
  private final Driver obmStorageDriver;
  private final DpsHeaders dpsHeaders;

  @Override
  public String getPersistentLocation(String relativePath, String partitionId) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
    return environmentResolver.getTransferProtocol(partitionId)
        + obmStorageUtil.getPersistentBucket(
        tenantInfo.getProjectId(), tenantInfo.getName()) + relativePath;
  }

  @Override
  public String getStagingLocation(String relativePath, String partitionId) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
    return environmentResolver.getTransferProtocol(partitionId) + obmStorageUtil.getStagingBucket(
        tenantInfo.getProjectId(), tenantInfo.getName()) + relativePath;
  }

  @Override
  public String getChecksum(String filePath) {
    if (Strings.isBlank(filePath)) {
      throw new OsduBadRequestException(String.format("Illegal file path argument - { %s }", filePath));
    }

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(dpsHeaders.getPartitionId());
    String fromBucket = obmStorageUtil.getBucketName(filePath, tenantInfo);
    String fromPath = obmStorageUtil.getDirectoryPath(filePath, tenantInfo);
    Blob sourceBlob = obmStorageDriver.getBlob(fromBucket, fromPath,
        obmStorageUtil.getDestination(tenantInfo.getDataPartitionId()));
    return sourceBlob.getChecksum();
  }

  @Override
  public ChecksumAlgorithm getChecksumAlgorithm() {
    return ChecksumAlgorithm.MD5;
  }
}
