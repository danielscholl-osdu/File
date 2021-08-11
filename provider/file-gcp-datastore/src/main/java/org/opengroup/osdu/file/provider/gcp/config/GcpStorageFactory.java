package org.opengroup.osdu.file.provider.gcp.config;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.GcsMultiTenantAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.cloud.storage.Storage;

@Component
public class GcpStorageFactory {

  @Autowired
  DpsHeaders headers;

  @Autowired
  private GcsMultiTenantAccess storageFactory;

  @Autowired
  private ITenantFactory tenantFactory;

  public Storage getStorage() {
    return storageFactory.get(tenantFactory.getTenantInfo(headers.getPartitionId()));
  }
}
