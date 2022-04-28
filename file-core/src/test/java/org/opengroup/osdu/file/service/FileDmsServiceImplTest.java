/*
 * Copyright 2021 Microsoft Corporation
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

package org.opengroup.osdu.file.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.file.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.file.TestUtils.PARTITION;


@ExtendWith(MockitoExtension.class)
public class FileDmsServiceImplTest {
  @Mock
  private IStorageService storageService;

  @Mock
  private DpsHeaders headers;

  @Mock
  private DataLakeStorageFactory storageFactory;

  @Mock
  private IStorageUtilService storageUtilService;

  @Mock
  private ICloudStorageOperation cloudStorageOperation;

  @Mock
  private StorageException storageException;

  @Mock
  private HttpResponse httpResponse;

  @Mock
  private List<FileCopyOperationResponse> fileCopyOperationResponses;

  @Mock
  private FileCopyOperationResponse fileCopyOperationResponse;

  @Mock
  private FileCopyOperation fileCopyOperation;

  @Mock
  private DataLakeStorageService dataLakeStorageService;

  private IDmsService dmsService;

  private static final String TEST_DATASET_ID = "opendes:dataset--File.Generic:foo-bar";
  private static final String TEST_FILE_SOURCE = "/osdu-user/foo-bar";
  private static final String TEST_UNSIGNED_URL = "https://blobstore/osdu-user/foo-bar";
  private static final String STAGING_LOCATION = "staging-location";
  private static final String PERSISTENT_LOCATION = "persistent-location";
  private static final String DATA_PARTITION_ID = "opendes";

  @BeforeEach
  void setUp() {
    dmsService = new FileDmsServiceImpl(storageService, headers, storageFactory, storageUtilService, cloudStorageOperation);
  }

  @Test
  void shouldReturnStorageInstructions() {
    // given
    Map<String, Object> storageLocation = new HashMap<>();
    storageLocation.put("signedUrl", "testSignedUrl");
    storageLocation.put("fileSource", "fileSource");

    StorageInstructionsResponse actualResponse = StorageInstructionsResponse.builder()
        .providerKey("AZURE").storageLocation(storageLocation).build();

    given(headers.getPartitionIdWithFallbackToAccountId()).willReturn(PARTITION);
    given(storageService.createStorageInstructions(anyString(), eq(PARTITION))).willReturn(actualResponse);


    // when
    StorageInstructionsResponse expectedResponse = dmsService.getStorageInstructions();

    // then
    then(expectedResponse).isEqualTo(actualResponse);
  }

  @Test
  void shouldReturnRetrievalInstructions() throws Exception {
    // given
    Map<String, Object> retrievalProperties = new HashMap<>();
    retrievalProperties.put("signedUrl", "testSignedUrl");
    retrievalProperties.put("fileSource", "fileSource");

    List<DatasetRetrievalProperties> datasets = new ArrayList<>();

    datasets.add(DatasetRetrievalProperties.builder()
        .retrievalProperties(retrievalProperties)
        .datasetRegistryId(TEST_DATASET_ID).build());

    RetrievalInstructionsResponse actualResponse = RetrievalInstructionsResponse.builder()
        .providerKey("AZURE").datasets(datasets).build();

    // test request.
    RetrievalInstructionsRequest testRequest = new RetrievalInstructionsRequest();
    testRequest.getDatasetRegistryIds().add(TEST_DATASET_ID);

    given(storageFactory.create(headers)).willReturn(dataLakeStorageService);

    // Multi record info
    List<Record> records = new ArrayList<>();
    addTestRecord(records);

    MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
    multiRecordInfo.setRecords(records);
    given(dataLakeStorageService.getRecords(testRequest.getDatasetRegistryIds())).willReturn(multiRecordInfo);

    given(headers.getPartitionId()).willReturn(PARTITION);

    given(storageUtilService.getPersistentLocation(TEST_FILE_SOURCE, PARTITION)).willReturn(TEST_UNSIGNED_URL);

    List<FileRetrievalData> testRetrieveData = new ArrayList<>();
    testRetrieveData.add(FileRetrievalData.builder()
        .recordId(TEST_DATASET_ID)
        .unsignedUrl(TEST_UNSIGNED_URL)
        .build());

    given(storageService.createRetrievalInstructions(testRetrieveData)).willReturn(actualResponse);

    // when
    RetrievalInstructionsResponse expectedResponse = dmsService.getRetrievalInstructions(testRequest);

    // then
    then(expectedResponse).isEqualTo(actualResponse);
  }

  @Test
  void shouldNotReturnRetrievalInstructionsWithInvalidMetadataRecord() throws Exception {
    // given
    Map<String, Object> retrievalProperties = new HashMap<>();
    retrievalProperties.put("signedUrl", "testSignedUrl");
    retrievalProperties.put("fileSource", "fileSource");

    List<DatasetRetrievalProperties> datasets = new ArrayList<>();

    datasets.add(DatasetRetrievalProperties.builder()
        .retrievalProperties(retrievalProperties)
        .datasetRegistryId(TEST_DATASET_ID).build());

    RetrievalInstructionsResponse actualResponse = RetrievalInstructionsResponse.builder()
        .providerKey("AZURE").datasets(datasets).build();

    // test request.
    RetrievalInstructionsRequest testRequest = new RetrievalInstructionsRequest();
    testRequest.getDatasetRegistryIds().add(TEST_DATASET_ID);

    given(storageFactory.create(headers)).willReturn(dataLakeStorageService);

    // Multi record info
    List<Record> records = new ArrayList<>();
    addTestInvalidRecord(records);

    MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
    multiRecordInfo.setRecords(records);
    given(dataLakeStorageService.getRecords(testRequest.getDatasetRegistryIds())).willReturn(multiRecordInfo);

    // when
    try {
      RetrievalInstructionsResponse expectedResponse = dmsService.getRetrievalInstructions(testRequest);
      fail("Method should throw exception");
    } catch (AppException e) {
      assertNotNull(e);
      assertEquals(HttpStatus.BAD_REQUEST.value(), e.getError().getCode());
    }
  }

  @Test
  void getRetrievalInstructionsStorageExceptionInDataLakeStorageGetRecords() throws Exception {
    RetrievalInstructionsRequest testRequest = new RetrievalInstructionsRequest();
    testRequest.getDatasetRegistryIds().add(TEST_DATASET_ID);

    when(storageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(dataLakeStorageService.getRecords(eq(testRequest.getDatasetRegistryIds()))).thenThrow(storageException);
    when(storageException.getHttpResponse()).thenReturn(httpResponse);
    when(httpResponse.getResponseCode()).thenReturn(500);
    when(storageException.getMessage()).thenReturn("exception");

    AppException exception = assertThrows(AppException.class, () -> {
      dmsService.getRetrievalInstructions(testRequest);
    });

    Assertions.assertNotNull(exception);
    Assertions.assertEquals(500, exception.getError().getCode());
    Assertions.assertEquals("Unable to fetch metadata for the datasets", exception.getError().getReason());
    Assertions.assertEquals("exception", exception.getError().getMessage());

    verify(storageFactory, times(1)).create(headers);
    verify(dataLakeStorageService, times(1)).getRecords(eq(testRequest.getDatasetRegistryIds()));
    verify(storageException, times(2)).getHttpResponse();
    verify(storageException, times(2)).getMessage();
    verify(httpResponse, times(1)).getResponseCode();

  }

  @Test
  public void copyDatasetsToPersistentLocationSuccess() {

    List<Record> records = new ArrayList<>();
    addTestRecord(records);

    when(fileCopyOperationResponses.get(0)).thenReturn(fileCopyOperationResponse);
    when(fileCopyOperationResponse.isSuccess()).thenReturn(true);
    when(cloudStorageOperation.copyFiles(any())).thenReturn(fileCopyOperationResponses);
    when(fileCopyOperationResponse.getCopyOperation()).thenReturn(fileCopyOperation);
    when(fileCopyOperation.getDestinationPath()).thenReturn(TEST_FILE_SOURCE);
    when(headers.getPartitionId()).thenReturn(DATA_PARTITION_ID);
    when(storageUtilService.getStagingLocation(TEST_FILE_SOURCE, DATA_PARTITION_ID)).thenReturn(STAGING_LOCATION);
    when(storageUtilService.getPersistentLocation(TEST_FILE_SOURCE, DATA_PARTITION_ID)).thenReturn(PERSISTENT_LOCATION);


    List<CopyDmsResponse> result = dmsService.copyDatasetsToPersistentLocation(records);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.size());
    Assertions.assertTrue(result.get(0).isSuccess());
    Assertions.assertEquals(TEST_FILE_SOURCE, result.get(0).getDatasetBlobStoragePath());

    verify(storageUtilService, times(1)).getPersistentLocation(TEST_FILE_SOURCE, DATA_PARTITION_ID);
    verify(storageUtilService, times(1)).getStagingLocation(TEST_FILE_SOURCE, DATA_PARTITION_ID);
    verify(headers, times(2)).getPartitionId();
    verify(cloudStorageOperation, times(1)).copyFiles(any());
    verify(fileCopyOperationResponses, times(2)).get(0);
    verify(fileCopyOperationResponse, times(1)).isSuccess();
    verify(fileCopyOperationResponse, times(1)).getCopyOperation();
    verify(fileCopyOperation, times(1)).getDestinationPath();

  }

  @Test
  public void copyDatasetsToPersistentLocationAppException() {

    List<Record> records = new ArrayList<>();
    addTestRecordNoDataSetProperty(records);

    AppException exception = assertThrows(AppException.class, () -> {
      dmsService.copyDatasetsToPersistentLocation(records);
    });

    Assertions.assertNotNull(exception);
    Assertions.assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, exception.getError().getCode());
    Assertions.assertEquals("Bad Request", exception.getError().getReason());
    Assertions.assertEquals("Dataset Metadata does not contain dataset properties", exception.getError().getMessage());

  }

  private void addTestRecordNoDataSetProperty(List<Record> records) {
    Record record = new Record();
    record.setId(TEST_DATASET_ID);

    Map<String, Object> data = new HashMap<>();
    record.setData(data);

    records.add(record);
  }

  private void addTestRecord(List<Record> records) {
    Record record = new Record();
    record.setId(TEST_DATASET_ID);

    Map<String, Object> data = new HashMap<>();
    DatasetProperties datasetProperties = new DatasetProperties();

    FileSourceInfo fileSourceInfo = new FileSourceInfo();
    fileSourceInfo.setFileSource(TEST_FILE_SOURCE);

    datasetProperties.setFileSourceInfo(fileSourceInfo);

    data.put("DatasetProperties", datasetProperties);
    record.setData(data);

    records.add(record);
  }

  private void addTestInvalidRecord(List<Record> records) {
    Record record = new Record();
    record.setId(TEST_DATASET_ID);

    Map<String, Object> data = new HashMap<>();
    DatasetProperties datasetProperties = new DatasetProperties();

    data.put("DatasetProperties", datasetProperties);
    record.setData(data);

    records.add(record);
  }

  private DpsHeaders getHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(DpsHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(DpsHeaders.DATA_PARTITION_ID, PARTITION);

    return DpsHeaders.createFromMap(headers);
  }

}
