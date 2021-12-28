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

package org.opengroup.osdu.file.provider.gcp.util.obm;

import static org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant.INVALID_S3_STORAGE_PATH_REASON;
import static org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant.MALFORMED_URL;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.obm.persistence.ObmDestination;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObmStorageUtil {

  private final EnvironmentResolver environmentResolver;
  private final PropertiesConfiguration propertiesConfiguration;

  public String getStagingBucket(String tenantProject) {
    return tenantProject + "-" + propertiesConfiguration.getStagingArea();
  }

  public String getPersistentBucket(String tenantProject) {
    return tenantProject + "-" + propertiesConfiguration.getPersistentArea();
  }


  public String getBucketName(String filePath, TenantInfo tenantInfo) {
    String[] gsPathParts =
        filePath.split(environmentResolver.getTransferProtocol(tenantInfo.getDataPartitionId()));

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_S3_STORAGE_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), MALFORMED_URL,
          INVALID_S3_STORAGE_PATH_REASON);
    }
    String bucketName = gsObjectKeyParts[0];
    return bucketName;
  }

  public String getDirectoryPath(String filePath, TenantInfo tenantInfo) {

    String result = "";
    String bucketName = getBucketName(filePath, tenantInfo);

    int initialIndex = filePath.indexOf(bucketName) + bucketName.length() + 1;
    int lastIndex = filePath.length();
    if ((lastIndex > 0) && (lastIndex > initialIndex)) {
      result = filePath.substring(initialIndex, lastIndex);
    }
    return result;
  }

  public ObmDestination getDestination(String partitionId) {
    return ObmDestination.builder().partitionId(partitionId).build();
  }

}
