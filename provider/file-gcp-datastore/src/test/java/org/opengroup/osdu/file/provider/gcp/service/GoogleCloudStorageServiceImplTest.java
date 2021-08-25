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

package org.opengroup.osdu.file.provider.gcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.gcp.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageServiceImplTest {

  private static final String PROVIDER_KEY = "GCP";
  private static final String PARTITION_ID = "partition";
  private static final String DATASET_ID = "dataset";
  private static final String USER_DES_ID = "user des id";
  private static final String BUCKET_NAME = "blobstore";
  private static final String FILE_PATH = "osdu-user/foo-bar";
  private static final String TEST_DATASET_ID = "opendes:dataset--File.Generic:foo-bar";
  private static final String TEST_UNSIGNED_URL = "gs://blobstore/osdu-user/foo-bar";
  private static final String CREATED_BY_ENTRY_KEY = "createdBy";
  private static final String SIGNED_URL_ENTRY_KEY = "signedUrl";
  private static final String SIGNED_URL_ENTRY_VALUE = "url";

  @InjectMocks
  GoogleCloudStorageServiceImpl storageService;

  @Spy
  ObjectMapper objectMapper;
  @Mock
  DpsHeaders dpsHeaders;
  @Mock
  ITenantFactory tenantFactory;
  @Mock
  GoogleCloudStorageUtil googleCloudStorageUtil;
  @Mock
  TenantInfo tenantInfo;
  @Mock
  FileLocationProperties fileLocationProperties;
  @Mock
  IStorageRepository storageRepository;
  @Mock
  SignedObject signedObject;
  @Mock
  URL url;

  Map<String, Object> storageLocation = new HashMap<>();
  Map<String, Object> retrievalProperties = new HashMap<>();

  @BeforeEach
  void setUp() {
    storageLocation.put(CREATED_BY_ENTRY_KEY, USER_DES_ID);
    storageLocation.put(SIGNED_URL_ENTRY_KEY, SIGNED_URL_ENTRY_VALUE);
    retrievalProperties.put(CREATED_BY_ENTRY_KEY, USER_DES_ID);
    retrievalProperties.put(SIGNED_URL_ENTRY_KEY, SIGNED_URL_ENTRY_VALUE);
  }

  @Test
  void createStorageInstructionsSuccess() {
    StorageInstructionsResponse expectedResponse = StorageInstructionsResponse.builder()
        .providerKey(PROVIDER_KEY)
        .storageLocation(storageLocation)
        .build();

    when(dpsHeaders.getAuthorization()).thenReturn(Strings.EMPTY);
    when(tenantFactory.getTenantInfo(any())).thenReturn(tenantInfo);
    when(googleCloudStorageUtil.getStagingBucket(any())).thenReturn(BUCKET_NAME);
    when(storageRepository.createSignedObject(eq(BUCKET_NAME), any())).thenReturn(signedObject);
    when(signedObject.getUrl()).thenReturn(url);
    when(fileLocationProperties.getUserId()).thenReturn(USER_DES_ID);

    StorageInstructionsResponse response =
        storageService.createStorageInstructions(DATASET_ID, PARTITION_ID);

    assertEquals(expectedResponse.getProviderKey(), response.getProviderKey());
    assertEquals(
        expectedResponse.getStorageLocation().get(SIGNED_URL_ENTRY_KEY),
        response.getStorageLocation().get(SIGNED_URL_ENTRY_KEY));
    assertEquals(
        expectedResponse.getStorageLocation().get(CREATED_BY_ENTRY_KEY),
        response.getStorageLocation().get(CREATED_BY_ENTRY_KEY));
  }

  @Test
  void createRetrievalInstructionsSuccess() {
    DatasetRetrievalProperties dataset = DatasetRetrievalProperties.builder()
        .retrievalProperties(retrievalProperties)
        .datasetRegistryId(TEST_DATASET_ID)
        .build();

    RetrievalInstructionsResponse expectedResponse =
        RetrievalInstructionsResponse.builder()
            .providerKey(PROVIDER_KEY)
            .datasets(Stream.of(dataset).collect(Collectors.toList()))
            .build();

    List<FileRetrievalData> fileRetrievalDataList =
        Stream.of(
                FileRetrievalData.builder()
                    .recordId(TEST_DATASET_ID)
                    .unsignedUrl(TEST_UNSIGNED_URL)
                    .build())
            .collect(Collectors.toList());

    when(storageRepository.getSignedObjectBasedOnParams(
            BUCKET_NAME, FILE_PATH, new SignedUrlParameters()))
        .thenReturn(signedObject);
    when(signedObject.getUrl()).thenReturn(url);
    when(fileLocationProperties.getUserId()).thenReturn(USER_DES_ID);

    RetrievalInstructionsResponse response =
        storageService.createRetrievalInstructions(fileRetrievalDataList);

    DatasetRetrievalProperties firstExpectedResponseRetrievalProperty =
        expectedResponse.getDatasets().get(0);
    DatasetRetrievalProperties firstResponseRetrievalProperty =
        response.getDatasets().get(0);

    assertEquals(expectedResponse.getProviderKey(), response.getProviderKey());
    assertEquals(
        firstExpectedResponseRetrievalProperty.getDatasetRegistryId(),
        firstResponseRetrievalProperty.getDatasetRegistryId());
    assertEquals(
        firstExpectedResponseRetrievalProperty.getRetrievalProperties().get(CREATED_BY_ENTRY_KEY),
        firstResponseRetrievalProperty.getRetrievalProperties().get(CREATED_BY_ENTRY_KEY));
    assertEquals(
        firstExpectedResponseRetrievalProperty.getRetrievalProperties().get(SIGNED_URL_ENTRY_KEY),
        firstResponseRetrievalProperty.getRetrievalProperties().get(SIGNED_URL_ENTRY_KEY));
  }
}
