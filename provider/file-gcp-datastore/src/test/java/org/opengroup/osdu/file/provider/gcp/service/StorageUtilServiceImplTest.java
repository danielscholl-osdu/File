package org.opengroup.osdu.file.provider.gcp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class StorageUtilServiceImplTest {

  @InjectMocks
  GoogleCloudStorageUtilServiceImpl googleCloudStorage;

  @Mock
  private GoogleCloudStorageUtil googleCloudStorageUtil;

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  private TenantFactory tenantFactory;

  @Mock
  DpsHeaders dpsHeaders;

  @Test
  public void getPersistentLocation() {
    String completePersistentFilePath  = "gs://tenant-project-persistent-area/some-area/some-folder/filename.txt";

    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn("tenant-project");
    when(googleCloudStorageUtil.getPersistentBucket(anyString())).thenReturn("tenant-project-persistent-area");
    String persistentFilePath = "/some-area/some-folder/filename.txt";
    assertEquals(completePersistentFilePath,
      googleCloudStorage.getPersistentLocation(persistentFilePath, "partition1"));
  }

  @Test
  public void getStagingLocation() {
    String completePersistentFilePath  = "gs://tenant-project-staging-area/some-area/some-folder/filename.txt";

    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn("tenant-project");
    when(googleCloudStorageUtil.getPersistentBucket(anyString())).thenReturn("tenant-project-staging-area");
    String persistentFilePath = "/some-area/some-folder/filename.txt";
    assertEquals(completePersistentFilePath,
      googleCloudStorage.getPersistentLocation(persistentFilePath, "partition1"));
  }

}
