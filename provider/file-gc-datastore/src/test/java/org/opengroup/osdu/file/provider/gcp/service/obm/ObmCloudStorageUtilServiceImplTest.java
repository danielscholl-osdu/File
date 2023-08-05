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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.obm.driver.EnvironmentResolver;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.file.provider.gcp.provider.service.ObmCloudStorageUtilServiceImpl;

@ExtendWith(MockitoExtension.class)
class ObmCloudStorageUtilServiceImplTest {

  public static final String EXPECTED_GC_PERSISTENT_PATH =
      "gs://tenant-project-tenant-name-staging-area/some-area/some-folder/filename.txt";
  public static final String EXPECTED_GS_STAGING_PATH =
      "gs://tenant-project-tenant-name-persistent-area/some-area/some-folder/filename.txt";
  public static final String EXPECTED_MINIO_PERSISTENT_PATH =
      "https://minio.com/tenant-project-tenant-name-persistent-area/some-area/some-folder/filename.txt";
  public static final String EXPECTED_MINIO_STAGING_PATH =
      "https://minio.com/tenant-project-tenant-name-staging-area/some-area/some-folder/filename.txt";
  public static final String TENANT_PROJECT = "tenant-project";
  public static final String TENANT_NAME = "tenant-name";
  public static final String PARTITION_ID = "partition1";
  public static final String MINIO_COM = "https://minio.com/";
  public static final String PERSISTENT_AREA = "persistent-area";
  public static final String STAGING_AREA = "staging-area";

  public static final String PERSISTENT_FILE_PATH = "/some-area/some-folder/filename.txt";
  public static final String GS_PROTOCOL = "gs://";

  @InjectMocks
  private ObmCloudStorageUtilServiceImpl obmCloudStorageUtilService;

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  private ITenantFactory tenantFactory;

  @Mock
  private EnvironmentResolver environmentResolver;
  @Mock
  private PropertiesConfiguration properties;

  @Test
  void getGSPersistentLocation() {
    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_PROJECT);
    when(tenantInfo.getName()).thenReturn(TENANT_NAME);
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(GS_PROTOCOL);
    when(properties.getPersistentArea()).thenReturn(PERSISTENT_AREA);

    assertEquals(EXPECTED_GS_STAGING_PATH,
        obmCloudStorageUtilService.getPersistentLocation(PERSISTENT_FILE_PATH, PARTITION_ID));
  }

  @Test
  void getGSStagingLocation() {
    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_PROJECT);
    when(tenantInfo.getName()).thenReturn(TENANT_NAME);
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(GS_PROTOCOL);
    when(properties.getPersistentArea()).thenReturn(STAGING_AREA);

    assertEquals(EXPECTED_GC_PERSISTENT_PATH,
        obmCloudStorageUtilService.getPersistentLocation(PERSISTENT_FILE_PATH, PARTITION_ID));
  }

  @Test
  void getMinioPersistentLocation() {
    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_PROJECT);
    when(tenantInfo.getName()).thenReturn(TENANT_NAME);
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_COM);
    when(properties.getPersistentArea()).thenReturn(PERSISTENT_AREA);

    assertEquals(EXPECTED_MINIO_PERSISTENT_PATH,
        obmCloudStorageUtilService.getPersistentLocation(PERSISTENT_FILE_PATH, PARTITION_ID));
  }

  @Test
  void getMinioStagingLocation() {
    when(tenantFactory.getTenantInfo(anyString())).thenReturn(tenantInfo);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_PROJECT);
    when(tenantInfo.getName()).thenReturn(TENANT_NAME);
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_COM);
    when(properties.getPersistentArea()).thenReturn(STAGING_AREA);

    assertEquals(EXPECTED_MINIO_STAGING_PATH,
        obmCloudStorageUtilService.getPersistentLocation(PERSISTENT_FILE_PATH, PARTITION_ID));
  }
}
