/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.provider.gcp.repository;

import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.osm.service.Context;
import org.opengroup.osdu.file.provider.gcp.config.GcpConfigurationProperties;
import org.opengroup.osdu.file.provider.gcp.provider.repository.OsmFileLocationRepository;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;

@ExtendWith(MockitoExtension.class)
class FileLocationRepositoryTest {

  private static final String FILE_ID = "file.id";
  public static final String TEST_PARTITION = "test";

  @Mock
  private Context osmDatabaseContext;

  private TenantInfo tenantInfo;

  private GcpConfigurationProperties configurationProperties;

  private IFileLocationRepository fileLocationRepository;

  @BeforeEach
  void setUp() {
    tenantInfo = new TenantInfo();
    tenantInfo.setDataPartitionId(TEST_PARTITION);
    tenantInfo.setName(TEST_PARTITION);

    configurationProperties = new GcpConfigurationProperties();
    configurationProperties.setFileLocationKind("file-test");

    fileLocationRepository
        = new OsmFileLocationRepository(osmDatabaseContext, configurationProperties, tenantInfo);
  }

  @Test
  void shouldReturnNullWhenFileIDNull() {
    FileLocation byFileID = fileLocationRepository.findByFileID(null);
    assertThatObject(byFileID).isNull();
  }

  @Test
  void shouldReturnNullWhenNoFileFoundByFileID() {
    when(osmDatabaseContext.getResultsAsList(any())).thenReturn(Collections.emptyList());
    FileLocation byFileID = fileLocationRepository.findByFileID(FILE_ID);
    assertThatObject(byFileID).isNull();
  }


}
