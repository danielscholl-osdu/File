package org.opengroup.osdu.file.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.exception.NotFoundException;
import org.opengroup.osdu.file.mapper.FileMetadataRecordMapper;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.FileMetadataResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.util.FileMetadataUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileMetadataServiceTest {

  public static final String RECORD_ID = "tenant1:file:1b9dd1a8-d317-11ea-87d0-0242ac130003";
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


  FileMetadata fileMetadata;

  @Test
  public void saveMetadata_Success() throws OsduBadRequestException, StorageException, ApplicationException {

    fileMetadata = FileMetadata.builder().data(FileData.builder().fileSource("stage/file.txt").build()).build();

    String dataPartitionId = "tenant";
    Record record = new Record(dataPartitionId);
    UpsertRecords upsertRecords = new UpsertRecords();
    List<String> recordIds = new ArrayList<>();
    recordIds.add(RECORD_ID);
    upsertRecords.setRecordIds(recordIds);


    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(fileMetadataUtil1.generateRecordId(dataPartitionId)).thenReturn(RECORD_ID);
    when(storageUtilService.getStagingLocation(any(), any())).thenReturn("root://stage/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(storageUtilService.getPersistentLocation(any(), any())).thenReturn("root://per/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
    when(dataLakeStorageService.upsertRecord(record)).thenReturn(upsertRecords);
    when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
    when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

    FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
    assertEquals(RECORD_ID, fileMetadataResponse.getId());

  }

  @Test
  public void saveMetadata_StorageException() throws OsduBadRequestException, StorageException, ApplicationException {

    fileMetadata = FileMetadata.builder().data(FileData.builder().fileSource("stage/file.txt").build()).build();

    String dataPartitionId = "tenant";
    Record record = new Record(dataPartitionId);
    UpsertRecords upsertRecords = new UpsertRecords();
    List<String> recordIds = new ArrayList<>();
    recordIds.add(RECORD_ID);
    upsertRecords.setRecordIds(recordIds);


    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(fileMetadataUtil1.generateRecordId(dataPartitionId)).thenReturn(RECORD_ID);
    when(storageUtilService.getStagingLocation(any(), any())).thenReturn("root://stage/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(storageUtilService.getPersistentLocation(any(), any())).thenReturn("root://per/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
    when(dataLakeStorageService.upsertRecord(record)).thenThrow(StorageException.class);
    when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
    when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

    assertThrows(StorageException.class,()->{
      FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
      assertEquals(RECORD_ID, fileMetadataResponse.getId());
    });


  }

  @Test
  public void saveMetadata_StorageFail() throws OsduBadRequestException, StorageException, ApplicationException {

    fileMetadata = FileMetadata.builder().data(FileData.builder().fileSource("stage/file.txt").build()).build();

    String dataPartitionId = "tenant";
    Record record = new Record(dataPartitionId);
    UpsertRecords upsertRecords = new UpsertRecords();
    List<String> recordIds = new ArrayList<>();
    recordIds.add(RECORD_ID);
    upsertRecords.setRecordIds(recordIds);


    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(fileMetadataUtil1.generateRecordId(dataPartitionId)).thenReturn(RECORD_ID);
    when(storageUtilService.getStagingLocation(any(), any())).thenReturn("root://stage/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(storageUtilService.getPersistentLocation(any(), any())).thenReturn("root://per/1b9dd1a8-d317-11ea-87d0-0242ac130003/fileName.txt");
    when(iFileMetadataRecordMapper.fileMetadataToRecord(any())).thenReturn(record);
    when(dataLakeStorageService.upsertRecord(record)).thenThrow(NullPointerException.class);
    when(cloudStorageOperation.copyFile(any(), any())).thenReturn("copy");
    when(cloudStorageOperation.deleteFile(any())).thenReturn(Boolean.TRUE);

    assertThrows(ApplicationException.class,()->{
      FileMetadataResponse fileMetadataResponse = fileMetadataService.saveMetadata(fileMetadata);
      assertEquals(RECORD_ID, fileMetadataResponse.getId());
    });
  }

  @Test
  public void getMetadataById_Success() throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {
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
  public void getMetadataById_StorageException() throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    HttpResponse httpResp = new HttpResponse();
    httpResp.setResponseCode(500);
    when(dataLakeStorageService.getRecord(RECORD_ID)).thenThrow(new StorageException("Failed to find record for the given file id", httpResp));

    assertThrows(ApplicationException.class,()->fileMetadataService.getMetadataById(RECORD_ID));
  }

  @Test
  public void getMetadataById_BadRequestException() throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    HttpResponse httpResp = new HttpResponse();
    httpResp.setResponseCode(400);
    when(dataLakeStorageService.getRecord(RECORD_ID)).thenThrow(new StorageException("Invalid file id", httpResp));

    assertThrows(OsduBadRequestException.class, ()->fileMetadataService.getMetadataById(RECORD_ID));
  }

  @Test
  public void getMetadataById_NotFoundException() throws OsduBadRequestException, NotFoundException, ApplicationException, StorageException {

    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    HttpResponse httpResp = new HttpResponse();
    httpResp.setResponseCode(400);
    when(dataLakeStorageService.getRecord(RECORD_ID)).thenReturn(null);

    assertThrows(NotFoundException.class,()->fileMetadataService.getMetadataById(RECORD_ID));
  }
}
