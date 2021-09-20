/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.di;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.multitenancy.PartitionTenantInfoFactory;
import org.opengroup.osdu.core.common.partition.IPartitionFactory;
import org.opengroup.osdu.core.common.partition.IPartitionProvider;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.googleidtoken.GcpServiceAccountJwtClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class PartitionTenantInfoFactoryBeanGcp extends AbstractFactoryBean<ITenantFactory> {
  @Value("${LOG_PREFIX}")
  private String LOG_PREFIX;
  @Autowired
  private IPartitionFactory partitionFactory;
  @Autowired
  private GcpServiceAccountJwtClient jwtClient;
  @Autowired(
      required = false
  )
  private ICache<String, TenantInfo> tenantCache;
  @Autowired
  private ILogger logger;

  public PartitionTenantInfoFactoryBeanGcp() {
  }

  public Class<?> getObjectType() {
    return PartitionTenantInfoFactory.class;
  }

  public ITenantFactory createInstance() {
    DpsHeaders partitionHeaders = new DpsHeaders();
    partitionHeaders.put("authorization", this.jwtClient.getDefaultOrInjectedServiceAccountIdToken());
    IPartitionProvider iPartitionProvider = this.partitionFactory.create(partitionHeaders);
    return PartitionTenantInfoFactory.builder().partitionProvider(iPartitionProvider).tenantCache(this.tenantCache).logger(this.logger).logPrefix(this.LOG_PREFIX).build();
  }
}
