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

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;
import org.opengroup.osdu.core.gcp.obm.driver.ObmPathProvider;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.core.gcp.obm.persistence.ObmDestination;
import org.opengroup.osdu.file.api.FileDmsApi;
import org.opengroup.osdu.file.constant.ChecksumAlgorithm;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.gcp.config.PartitionPropertyNames;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.stereotype.Component;

/**
 * Get cloud storage staging and persistent location directory path.
 * Used for getRetrievalInstructions and copyDatasetsToPersistentLocation operations {@link FileDmsApi}.
 * Directory paths are generated based on `gcp.storage.stagingArea` and `gcp.storage.persistentArea properties`.
 */
@Component
@RequiredArgsConstructor
public class ObmCloudStorageUtilServiceImpl implements IStorageUtilService {

  private final ITenantFactory tenantFactory;
  private final ObmPathProvider pathProvider;
  private final EnvironmentResolver environmentResolver;
  private final Driver obmStorageDriver;
  private final DpsHeaders dpsHeaders;
  private final PropertiesConfiguration properties;
  private final PartitionPropertyNames partitionPropertyNames;
  private final PartitionPropertyResolver partitionPropertyResolver;

  @Override
  public String getPersistentLocation(String relativePath, String partitionId) {
    return partitionPropertyResolver.getOptionalPropertyValue(partitionPropertyNames.getPersistentLocationName(), partitionId).orElseGet(() -> {
      TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
      return String.format("%s%s-%s-%s%s", environmentResolver.getTransferProtocol(partitionId),
          tenantInfo.getProjectId(), tenantInfo.getName(), properties.getPersistentArea(), relativePath);
    });
  }

  @Override
  public String getStagingLocation(String relativePath, String partitionId) {
    return partitionPropertyResolver.getOptionalPropertyValue(partitionPropertyNames.getStagingLocationName(), partitionId).orElseGet(() -> {
      TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
      return String.format("%s%s-%s-%s%s", environmentResolver.getTransferProtocol(partitionId),
          tenantInfo.getProjectId(), tenantInfo.getName(), properties.getStagingArea(), relativePath);
    });
  }

  @Override
  public String getChecksum(String filePath) {
    if (Strings.isBlank(filePath)) {
      throw new OsduBadRequestException(String.format("Illegal file path argument - { %s }", filePath));
    }

    String partitionId = dpsHeaders.getPartitionId();
    String fromBucket = pathProvider.extractBucketInfoFromUnsignedUrl(filePath, partitionId).getBucketName();
    String fromPath = pathProvider.getDirectoryPath(filePath, partitionId);
    ObmDestination obmDestination = ObmDestination.builder().partitionId(partitionId).build();
    Blob sourceBlob = obmStorageDriver.getBlob(fromBucket, fromPath, obmDestination);
    return sourceBlob.getChecksum();
  }

  @Override
  public ChecksumAlgorithm getChecksumAlgorithm() {
    return ChecksumAlgorithm.MD5;
  }
}
