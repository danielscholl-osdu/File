package org.opengroup.osdu.file.provider.gcp.service;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoogleCloudStorageUtilServiceImpl implements IStorageUtilService {

  @Autowired
  ITenantFactory tenantFactory;

  @Autowired
  GoogleCloudStorageUtil googleCloudStorageUtil;

  @Override
  public String getPersistentLocation(String relativePath, String partitionId) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
    return "gs://" + googleCloudStorageUtil.getPersistentBucket(tenantInfo.getProjectId()) + relativePath;
  }

  @Override
  public String getStagingLocation(String relativePath, String partitionId) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionId);
    return "gs://" +  googleCloudStorageUtil.getStagingBucket(tenantInfo.getProjectId()) + relativePath;
  }

}
