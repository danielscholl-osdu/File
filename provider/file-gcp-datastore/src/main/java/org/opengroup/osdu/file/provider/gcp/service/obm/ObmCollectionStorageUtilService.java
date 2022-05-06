/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.service.obm;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.util.obm.ObmStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageUtilService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class ObmCollectionStorageUtilService implements IFileCollectionStorageUtilService {

  private final ITenantFactory tenantFactory;
  private final ObmStorageUtil obmStorageUtil;
  private final EnvironmentResolver environmentResolver;

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
}
