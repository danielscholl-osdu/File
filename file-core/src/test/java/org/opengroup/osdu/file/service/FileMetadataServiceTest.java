// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.KindValidationException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.exception.NotFoundException;
import org.opengroup.osdu.file.mapper.FileMetadataRecordMapper;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.FileMetadataResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.status.FileDatasetDetailsPublisher;
import org.opengroup.osdu.file.service.status.FileStatusPublisher;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.util.FileMetadataUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class FileMetadataServiceTest {

    public static final String RECORD_ID = "tenant1:dataset--File.Generic:1b9dd1a8-d317-11ea-87d0-0242ac130003";
    public static final String RECORD_ID_VERSION = "tenant1:dataset--File.Generic:1b9dd1a8-d317-11ea-87d0-0242ac130003:123456789";
    public static final String FILE_METADATA_KIND = "osdu:wks:dataset--File.Generic:1.0.0";
    private static final String STAGING_FILE_PATH = "root://stage/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt";

    private static final String PERSISTENT_FILE_PATH = "root://per/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt";
    private static final String FILE_SOURCE = "stage/file.txt";
    @InjectMocks
    FileMetadataService fileMetadataService;

    @Mock
    DataLakeStorageFactory dataLakeStorageFactory;

    @Mock
    DataLakeStorageService dataLakeStorageService;

    @Mock
    DpsHeaders headers;

    @Mock
    JaxRsDpsLog log;

    @Mock
    ICloudStorageOperation cloudStorageOperation;

    @Mock
    FileMetadataUtil fileMetadataUtil1;

    @Mock
    FileMetadataRecordMapper iFileMetadataRecordMapper;

    @Mock
    IStorageUtilService storageUtilService;

    @Mock
    FileStatusPublisher fileStatusPublisher;

    @Mock
    FileDatasetDetailsPublisher fileDatasetDetailsPublisher;

    FileMetadata fileMetadata;

    @Test
    public void saveMetadata_Success() throws OsduBadRequestException, StorageException, ApplicationException {

        FileSourceInfo fileSourceInfo = FileSourceInfo.builder().fileSource(FILE_SOURCE).build();
        DatasetProperties datasetProperties = DatasetProperties.builder().fileSourceInfo(fileSourceInfo).build();
        FileData fileData = FileData.builder().datasetProperties(datasetProperties).build();

        fileMetadata = FileMetadata.builder().data(fileData).kind(FILE_METADATA_KIND).build();

        String dataPartitionId = "tenant";
        Record record = new Record(dataPartitionId);
        UpsertRecords upsertRecords = new UpsertRecords();
        List<String> recordIds = new ArrayList<>();
        recordIds.add(RECORD_ID);
        List<String> recordIdVersions = new ArrayList<>();
        recordIdVersions.add(RECORD_ID_VERSION);
        upsertRecords.setRecordIds(recordIds);
        upsertRecords.setRecordIdVersions(recordIdVersions);

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        when(fileMetadataUtil1.generateRecordId(anyString(), anyString())).thenReturn(RECORD_ID);
        when(storageUtilService.getStagingLocation(any(), any()))
                .thenReturn(STAGING_FILE_PATH);
        when(storageUtilService.getPersistentLocation(any(), any()))
                .thenReturn(PERSISTENT_FILE_PATH);
        when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
        when(dataLakeStorageService.upsertRecord(record)).thenReturn(upsertRecords);
        when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
        when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

        FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
        assertEquals(RECORD_ID, fileMetadataResponse.getId());

    }
  @Test
  public void saveMetadata_StagingFileNotDeletedPrematurely() throws OsduBadRequestException, StorageException, ApplicationException {

    FileSourceInfo fileSourceInfo = FileSourceInfo.builder().fileSource(FILE_SOURCE).build();
    DatasetProperties datasetProperties = DatasetProperties.builder().fileSourceInfo(fileSourceInfo).build();
    FileData fileData = FileData.builder().datasetProperties(datasetProperties).build();

    fileMetadata = FileMetadata.builder().data(fileData).kind(FILE_METADATA_KIND).build();

    String dataPartitionId = "tenant";
    Record record = new Record(dataPartitionId);
    UpsertRecords upsertRecords = new UpsertRecords();
    List<String> recordIds = new ArrayList<>();
    recordIds.add(RECORD_ID);
    List<String> recordIdVersions = new ArrayList<>();
    recordIdVersions.add(RECORD_ID_VERSION);
    upsertRecords.setRecordIds(recordIds);
    upsertRecords.setRecordIdVersions(recordIdVersions);

    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(fileMetadataUtil1.generateRecordId(anyString(), anyString())).thenReturn(RECORD_ID);
    when(storageUtilService.getStagingLocation(any(), any()))
        .thenReturn(STAGING_FILE_PATH);
    when(storageUtilService.getPersistentLocation(any(), any()))
        .thenReturn(PERSISTENT_FILE_PATH);
    when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
    when(dataLakeStorageService.upsertRecord(record)).thenReturn(upsertRecords);
    when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");

    doThrow(RuntimeException.class).when(fileStatusPublisher).publishSuccessStatus(anyString(),anyString());

    /**Verify, if the  publisher threw runtime exception we get application exception as the resultant exception**/
    assertThrows(ApplicationException.class, () -> {
      FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
      assertEquals(RECORD_ID, fileMetadataResponse.getId());
    });

    /**
     * Verify staging file path is not deleted.
     * */
    verify(cloudStorageOperation, never()).deleteFile(STAGING_FILE_PATH);


    /**Second attempt**/
    /**Verify, if the  publisher didn't throw exception we are able to save the metadata successfully this time**/
    doNothing().when(fileStatusPublisher).publishSuccessStatus(anyString(),anyString());
    when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

    FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
    assertEquals(RECORD_ID, fileMetadataResponse.getId());
    /**
     * Verify staging file path is deleted this time.
     * */
    verify(cloudStorageOperation, atMostOnce()).deleteFile(STAGING_FILE_PATH);

  }
    @Test
    public void saveMetadata_StorageException() throws OsduBadRequestException, StorageException, ApplicationException {

        FileSourceInfo fileSourceInfo = FileSourceInfo.builder().fileSource(FILE_SOURCE).build();
        DatasetProperties datasetProperties = DatasetProperties.builder().fileSourceInfo(fileSourceInfo).build();
        FileData fileData = FileData.builder().datasetProperties(datasetProperties).build();

        fileMetadata = FileMetadata.builder().kind(FILE_METADATA_KIND).data(fileData).build();

        String dataPartitionId = "tenant";
        Record record = new Record(dataPartitionId);
        UpsertRecords upsertRecords = new UpsertRecords();
        List<String> recordIds = new ArrayList<>();
        recordIds.add(RECORD_ID);
        upsertRecords.setRecordIds(recordIds);

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        when(fileMetadataUtil1.generateRecordId(anyString(), anyString())).thenReturn(RECORD_ID);
        when(storageUtilService.getStagingLocation(any(), any()))
                .thenReturn(STAGING_FILE_PATH);
        when(storageUtilService.getPersistentLocation(any(), any()))
                .thenReturn(PERSISTENT_FILE_PATH);
        when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
        when(dataLakeStorageService.upsertRecord(record)).thenThrow(StorageException.class);
        when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
        when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

        assertThrows(StorageException.class, () -> {
            FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
            assertEquals(RECORD_ID, fileMetadataResponse.getId());
        });

    }

    @Test
    public void saveMetadata_InvalidKind() throws OsduBadRequestException, StorageException, ApplicationException {
        fileMetadata = FileMetadata.builder().kind("invalidKind").build();

        KindValidationException exceptionResponse = assertThrows(KindValidationException.class, () -> {
            fileMetadataService.saveMetadata(fileMetadata);
        });

        assertEquals("Invalid kind", exceptionResponse.getMessage());

        fileMetadata = FileMetadata.builder().kind("osdu:invalidSource:dataset--File.Generic:1.0.0").build();

        exceptionResponse = assertThrows(KindValidationException.class, () -> {
            fileMetadataService.saveMetadata(fileMetadata);
        });

        assertEquals("Invalid source in kind", exceptionResponse.getMessage());

        fileMetadata = FileMetadata.builder().kind("osdu:wks:invalidEntity:1.0.0").build();

        exceptionResponse = assertThrows(KindValidationException.class, () -> {
            fileMetadataService.saveMetadata(fileMetadata);
        });

        assertEquals("Invalid entity in kind", exceptionResponse.getMessage());
    }

    @Test
    public void saveMetadata_StorageFail() throws OsduBadRequestException, StorageException, ApplicationException {

        FileSourceInfo fileSourceInfo = FileSourceInfo.builder().fileSource(FILE_SOURCE).build();
        DatasetProperties datasetProperties = DatasetProperties.builder().fileSourceInfo(fileSourceInfo).build();
        FileData fileData = FileData.builder().datasetProperties(datasetProperties).build();

        fileMetadata = FileMetadata.builder().kind(FILE_METADATA_KIND).data(fileData).build();

        String dataPartitionId = "tenant";
        Record record = new Record(dataPartitionId);
        UpsertRecords upsertRecords = new UpsertRecords();
        List<String> recordIds = new ArrayList<>();
        recordIds.add(RECORD_ID);
        upsertRecords.setRecordIds(recordIds);

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        when(fileMetadataUtil1.generateRecordId(anyString(), anyString())).thenReturn(RECORD_ID);
        when(storageUtilService.getStagingLocation(any(), any()))
                .thenReturn(STAGING_FILE_PATH);
        when(storageUtilService.getPersistentLocation(any(), any()))
                .thenReturn(PERSISTENT_FILE_PATH);
        when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
        when(dataLakeStorageService.upsertRecord(record)).thenThrow(NullPointerException.class);
        when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
        when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

        assertThrows(ApplicationException.class, () -> {
            FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
            assertEquals(RECORD_ID, fileMetadataResponse.getId());
        });
    }

    @Test
    public void getMetadataById_Success()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {
        String id = "tenant1:file:efa0d783-5b67-4bda-b172-7ce426a58d90";
        Record mockRecord = new Record("tenant1");
        mockRecord.setId(RECORD_ID);

        RecordVersion mockRecordVersion = new RecordVersion();
        mockRecordVersion.setId(RECORD_ID);

        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        when(dataLakeStorageService.getRecord(id)).thenReturn(mockRecord);
        when(iFileMetadataRecordMapper.recordToRecordVersion(any())).thenReturn(mockRecordVersion);

        RecordVersion recordVersion = fileMetadataService.getMetadataById(id);

        assertEquals(RECORD_ID, recordVersion.getId());
    }

    @Test
    public void getMetadataById_StorageException()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        HttpResponse httpResp = new HttpResponse();
        httpResp.setResponseCode(500);
        when(dataLakeStorageService.getRecord(RECORD_ID))
                .thenThrow(new StorageException("Failed to find record for the given file id", httpResp));

        assertThrows(StorageException.class, () -> fileMetadataService.getMetadataById(RECORD_ID));
    }

    @Test
    public void getMetadataById_BadRequestException()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        HttpResponse httpResp = new HttpResponse();
        httpResp.setResponseCode(400);
        when(dataLakeStorageService.getRecord(RECORD_ID)).thenThrow(new StorageException("Invalid file id", httpResp));

        assertThrows(StorageException.class, () -> fileMetadataService.getMetadataById(RECORD_ID));
    }

    @Test
    public void getMetadataById_NotFoundException()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        HttpResponse httpResp = new HttpResponse();
        httpResp.setResponseCode(400);
        when(dataLakeStorageService.getRecord(RECORD_ID)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> fileMetadataService.getMetadataById(RECORD_ID));
    }

    @Test
    public void deleteMetadataRecord_Success()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {
        Record mockRecord = new Record("tenant1");
        mockRecord.setId(RECORD_ID);
        HttpResponse result = new HttpResponse();
        result.setResponseCode(204);
        FileMetadataService spyFileMetadataService = Mockito.spy(fileMetadataService);
        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        Mockito.doReturn(getRecordVersionObj()).when(spyFileMetadataService).getMetadataById(RECORD_ID);
        when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);
        when(dataLakeStorageService.deleteRecord(RECORD_ID)).thenReturn(result);

        spyFileMetadataService.deleteMetadataRecord(RECORD_ID);

        assertEquals(FileMetadataConstant.HTTP_CODE_204, result.getResponseCode());
    }

    @Test
    public void deleteMetadataRecord_Exception()
            throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

        Record mockRecord = new Record("tenant1");
        mockRecord.setId(RECORD_ID);

        FileMetadataService spyFileMetadataService = Mockito.spy(fileMetadataService);
        when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
        Mockito.doReturn(getRecordVersionObj()).when(spyFileMetadataService).getMetadataById(RECORD_ID);
        when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);
        HttpResponse httpResp = new HttpResponse();
        httpResp.setResponseCode(500);

        when(dataLakeStorageService.deleteRecord(RECORD_ID)).thenReturn(httpResp);

        assertThrows(StorageException.class, () -> spyFileMetadataService.deleteMetadataRecord(RECORD_ID));
    }

    private RecordVersion getRecordVersionObj() {
        RecordVersion mockRecordVersion = new RecordVersion();
        FileData fileData = new FileData();
        DatasetProperties datasetProperties = new DatasetProperties();
        FileSourceInfo fileSourceInfo = new FileSourceInfo();
        fileSourceInfo.setFileSource("/xyz");
        datasetProperties.setFileSourceInfo(fileSourceInfo);
        fileData.setDatasetProperties(datasetProperties);
        mockRecordVersion.setData(fileData);
        mockRecordVersion.setId(RECORD_ID);
        return mockRecordVersion;
    }

}
