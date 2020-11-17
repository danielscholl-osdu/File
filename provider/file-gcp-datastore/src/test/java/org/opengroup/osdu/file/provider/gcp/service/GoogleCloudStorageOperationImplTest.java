package org.opengroup.osdu.file.provider.gcp.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.multitenancy.GcsMultiTenantAccess;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class GoogleCloudStorageOperationImplTest {

  @InjectMocks
  GoogleCloudStorageOperationImpl storage;

  @Mock
  DpsHeaders headers;

  @Mock
  JaxRsDpsLog log;

  @Mock
  Blob mockBlob;


  @Mock
  Storage mockStorage;

  @Mock
  CopyWriter mockCopyWriter;

  @Mock
  TenantFactory tenantFactory;

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  GcsMultiTenantAccess storageFactory;

  @Mock
  GoogleCloudStorageUtil googleCloudStorageUtil;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    storage = new GoogleCloudStorageOperationImpl(headers, storageFactory, tenantFactory, googleCloudStorageUtil,log);
  }

  @Test
  public void copyFileSuccess() throws OsduBadRequestException {
    String fromFile = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    String toFile = "gs://to-google-bucket/some-area/some-folder/filename.txt";

    BlobId sourceBlobId = Blob.newBuilder("from-google-bucket", "some-area/some-folder/filename.txt").build().getBlobId();
    Blob mockDestBlob = mock(Blob.class);
    when(googleCloudStorageUtil.getBucketName(fromFile)).thenReturn("from-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(fromFile)).thenReturn("some-area/some-folder/filename.txt");
    when(googleCloudStorageUtil.getBucketName(toFile)).thenReturn("to-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(toFile)).thenReturn("some-area/some-folder/filename.txt");
    when(googleCloudStorageUtil.getCompleteFilePath("to-google-bucket","some-area/some-folder/filename.txt" ))
      .thenReturn(toFile);

    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);
    when(mockStorage.get(anyString(), anyString())).thenReturn(mockBlob);
    when(mockBlob.getBlobId()).thenReturn(sourceBlobId);
    when(mockStorage.copy(any())).thenReturn(mockCopyWriter);
    when(mockCopyWriter.getResult()).thenReturn(mockDestBlob);
    when(mockDestBlob.getName()).thenReturn("some-area/some-folder/filename.txt");

    String blobName = storage.copyFile(fromFile, toFile);

    assertTrue((toFile).equals(blobName));


  }

  @Test
  public void copyFileBadRequest()  {
    String fromFile = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    String toFile = "";

    BlobId sourceBlobId = Blob.newBuilder("from-google-bucket", "some-area/some-folder/filename.txt").build().getBlobId();
    Blob mockDestBlob = mock(Blob.class);
    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);
    when(mockStorage.get("from-google-bucket", "some-area/some-folder/filename.txt")).thenReturn(mockBlob);
    when(mockBlob.getBlobId()).thenReturn(sourceBlobId);
    when(mockStorage.copy(any())).thenReturn(mockCopyWriter);
    when(mockCopyWriter.getResult()).thenReturn(mockDestBlob);
    when(mockDestBlob.getName()).thenReturn("some-area/some-folder/filename.txt");

    Assertions.assertThrows(OsduBadRequestException.class, () -> storage.copyFile(fromFile, toFile));
  }

  @Test
  public void deleteFile() {
    String location = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    BlobId sourceBlobId = Blob.newBuilder("from-google-bucket", "some-area/some-folder/filename.txt").build().getBlobId();
    Blob mockBlob = mock(Blob.class);
    when(googleCloudStorageUtil.getBucketName(any())).thenReturn("from-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(any())).thenReturn("some-area/some-folder/filename.txt");
    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);
    when(mockStorage.get("from-google-bucket", "some-area/some-folder/filename.txt")).thenReturn(mockBlob);
    when(mockBlob.getBlobId()).thenReturn(sourceBlobId);
    when(mockStorage.delete(sourceBlobId)).thenReturn(true);
    when(mockBlob.getName()).thenReturn("some-area/some-folder/filename.txt");
    assertTrue(storage.deleteFile(location));
  }

  @Test
  public void throwBadRequest() throws OsduBadRequestException {
    Assertions.assertThrows(OsduBadRequestException.class,
                            () -> storage.throwBadRequest("Error occurred"));
  }




}
