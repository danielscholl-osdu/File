package org.opengroup.osdu.file.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.filecollection.DatasetProperties;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.file.TestUtils.PARTITION;

@ExtendWith(MockitoExtension.class)
public class FileCollectionDmsServiceImplTest {

  private static final String SIGNED_URL = "signedUrl";
  private static final String TEST_SIGNED_URL= "testSignedUrl";
  private static final String FILE_COLLECTION_SOURCE = "fileCollectionSource";
  private static final String AZURE = "azure";
  private static final String TEST_DATASET_ID = "opendes:dataset--File.Generic:foo-bar";
  private static final String TEST_FILE_COLLECTION_SOURCE = "osdu-user-1641762126717";
  private static final String TEST_UNSIGNED_URL = "https://datalakestore/fileSystemName/osdu-user-1641762126717";

  @Mock
  @Qualifier("FileCollectionStorageService")
  IStorageService storageService;

  @Mock
  DpsHeaders headers;

  @Mock
  DataLakeStorageFactory storageFactory;

  @Mock
  ICloudStorageOperation cloudStorageOperation;

  @Mock
  @Qualifier("FileCollectionUtilService")
  IStorageUtilService storageUtilService;

  @Mock
  DataLakeStorageService dataLakeStorageService;

  @InjectMocks
  FileCollectionDmsServiceImpl fileCollectionDmsService;

  @Test
  public void ShouldReturnStorageInstructions() {
    // given
    Map<String, Object> storageLocation = new HashMap<>();
    storageLocation.put(SIGNED_URL, TEST_SIGNED_URL);
    storageLocation.put(FILE_COLLECTION_SOURCE, FILE_COLLECTION_SOURCE);

    StorageInstructionsResponse actualResponse = StorageInstructionsResponse.builder()
        .providerKey(AZURE).storageLocation(storageLocation).build();

    given(headers.getPartitionIdWithFallbackToAccountId()).willReturn(PARTITION);
    given(storageService.createStorageInstructions(anyString(), eq(PARTITION))).willReturn(actualResponse);

    // when
    StorageInstructionsResponse expectedResponse = fileCollectionDmsService.getStorageInstructions();

    // then
    then(expectedResponse).isEqualTo(actualResponse);
  }

  @Test
  void shouldReturnRetrievalInstructions() throws Exception {
    //Mock
    RetrievalInstructionsResponse actualResponse = prepareRetrievalInstructionsResponse();
    RetrievalInstructionsRequest testRequest = prepareRetrievalInstructionsRequest();
    given(headers.getPartitionId()).willReturn(PARTITION);
    given(storageUtilService.getPersistentLocation(TEST_FILE_COLLECTION_SOURCE, PARTITION)).willReturn(TEST_UNSIGNED_URL);

    List<FileRetrievalData> testRetrieveData = new ArrayList<>();
    testRetrieveData.add(FileRetrievalData.builder()
        .recordId(TEST_DATASET_ID)
        .unsignedUrl(TEST_UNSIGNED_URL)
        .build());

    given(storageService.createRetrievalInstructions(testRetrieveData)).willReturn(actualResponse);

    // call
    RetrievalInstructionsResponse expectedResponse = fileCollectionDmsService.getRetrievalInstructions(testRequest);

    // Assert
    then(expectedResponse).isEqualTo(actualResponse);
  }

  @Test
  void shouldNotReturnRetrievalInstructionsWithInvalidMetadataRecord() throws Exception {
    //Mock
    prepareRetrievalInstructionsResponse();
    RetrievalInstructionsRequest testRequest = prepareRetrievalInstructionsRequestForInvalidRecords();

    // call
    try {
      fileCollectionDmsService.getRetrievalInstructions(testRequest);
      fail("Method should throw exception");
    } catch (AppException e) {

      //Assert
      assertNotNull(e);
      assertEquals(HttpStatus.BAD_REQUEST.value(), e.getError().getCode());
    }
  }

  private RetrievalInstructionsRequest prepareRetrievalInstructionsRequest() throws StorageException {
    RetrievalInstructionsRequest  testRequest= new RetrievalInstructionsRequest();
    testRequest.getDatasetRegistryIds().add(TEST_DATASET_ID);

    when(storageFactory.create(headers)).thenReturn(dataLakeStorageService);
    // Multi record info
    List<Record> records = new ArrayList<>();
    addTestRecord(records);

    MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
    multiRecordInfo.setRecords(records);
    given(dataLakeStorageService.getRecords(testRequest.getDatasetRegistryIds())).willReturn(multiRecordInfo);

    return testRequest;
  }

  private RetrievalInstructionsRequest prepareRetrievalInstructionsRequestForInvalidRecords() throws StorageException {
    RetrievalInstructionsRequest  testRequest= new RetrievalInstructionsRequest();
    testRequest.getDatasetRegistryIds().add(TEST_DATASET_ID);

    when(storageFactory.create(headers)).thenReturn(dataLakeStorageService);
    // Multi record info
    List<Record> records = new ArrayList<>();
    addTestInvalidRecord(records);

    MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
    multiRecordInfo.setRecords(records);
    given(dataLakeStorageService.getRecords(testRequest.getDatasetRegistryIds())).willReturn(multiRecordInfo);

    return testRequest;
  }

  private RetrievalInstructionsResponse prepareRetrievalInstructionsResponse() {
    Map<String, Object> retrievalProperties = new HashMap<>();
    retrievalProperties.put(SIGNED_URL, TEST_SIGNED_URL);
    retrievalProperties.put(FILE_COLLECTION_SOURCE, FILE_COLLECTION_SOURCE);

    List<DatasetRetrievalProperties> datasets = new ArrayList<>();

    datasets.add(DatasetRetrievalProperties.builder()
        .retrievalProperties(retrievalProperties)
        .datasetRegistryId(TEST_DATASET_ID).build());


    RetrievalInstructionsResponse actualResponse = RetrievalInstructionsResponse.builder()
        .providerKey("AZURE").datasets(datasets).build();

    return actualResponse;
  }

  private void addTestRecord(List<Record> records) {
    Record record = new Record();
    record.setId(TEST_DATASET_ID);

    Map<String, Object> data = new HashMap<>();
    DatasetProperties datasetProperties = new DatasetProperties();

    datasetProperties.setFileCollectionPath(TEST_FILE_COLLECTION_SOURCE);

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
}
