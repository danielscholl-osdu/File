package org.opengroup.osdu.file.provider.gcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.Storage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.multitenancy.GcsMultiTenantAccess;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class GoogleCloudStorageOperationImplTest {

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

  @Test
  void copyFileSuccess() throws OsduBadRequestException {
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

    assertEquals((toFile), blobName);
  }

  @Test
  void copyFileBadRequest()  {
    String fromFile = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    String toFile = "";

    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);

    Assertions.assertThrows(OsduBadRequestException.class, () -> storage.copyFile(fromFile, toFile));
  }

  @Test
  void copyFilesSuccess() {
    String fromFileFirst = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    String toFileFirst = "gs://to-google-bucket/some-area/some-folder/filename.txt";

    String fromFileSecond = "gs://from-google-bucket/some-area/some-folder/otherfile.txt";
    String toFileSecond = "gs://to-google-bucket/some-area/some-folder/otherfile.txt";

    FileCopyOperation fileCopyOperationFirst = FileCopyOperation.builder()
        .sourcePath(fromFileFirst)
        .destinationPath(toFileFirst)
        .build();

    FileCopyOperation fileCopyOperationSecond = FileCopyOperation.builder()
        .sourcePath(fromFileSecond)
        .destinationPath(toFileSecond)
        .build();

    List<FileCopyOperationResponse> expectedList =
        Stream.of(
                FileCopyOperationResponse.builder()
                    .copyOperation(fileCopyOperationFirst)
                    .success(Boolean.TRUE)
                    .build(),
                FileCopyOperationResponse.builder()
                    .copyOperation(fileCopyOperationSecond)
                    .success(Boolean.TRUE)
                    .build())
            .collect(Collectors.toList());

    BlobId sourceBlobId = Blob.newBuilder("from-google-bucket", "some-area/some-folder/filename.txt").build().getBlobId();
    Blob mockDestBlob = mock(Blob.class);
    when(googleCloudStorageUtil.getBucketName(fromFileFirst)).thenReturn("from-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(fromFileFirst)).thenReturn("some-area/some-folder/filename.txt");
    when(googleCloudStorageUtil.getBucketName(toFileFirst)).thenReturn("to-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(toFileFirst)).thenReturn("some-area/some-folder/filename.txt");
    when(googleCloudStorageUtil.getCompleteFilePath("to-google-bucket","some-area/some-folder/filename.txt" ))
        .thenReturn(toFileFirst);

    when(googleCloudStorageUtil.getBucketName(fromFileSecond)).thenReturn("from-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(fromFileSecond)).thenReturn("some-area/some-folder/otherfile.txt");
    when(googleCloudStorageUtil.getBucketName(toFileSecond)).thenReturn("to-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(toFileSecond)).thenReturn("some-area/some-folder/otherfile.txt");

    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);
    when(mockStorage.get(anyString(), anyString())).thenReturn(mockBlob);
    when(mockBlob.getBlobId()).thenReturn(sourceBlobId);
    when(mockStorage.copy(any())).thenReturn(mockCopyWriter);
    when(mockCopyWriter.getResult()).thenReturn(mockDestBlob);
    when(mockDestBlob.getName()).thenReturn("some-area/some-folder/filename.txt");

    List<FileCopyOperationResponse> fileCopyOperationResponseList =
        storage.copyFiles(Stream.of(fileCopyOperationFirst, fileCopyOperationSecond)
            .collect(Collectors.toList()));

    assertEquals(expectedList, fileCopyOperationResponseList);
  }

  @Test
  void copyFilesBadRequest() {
    FileCopyOperation fileCopyOperationFirst = FileCopyOperation.builder()
        .sourcePath("gs://from-google-bucket/some-area/some-folder/filename.txt")
        .destinationPath("")
        .build();

    FileCopyOperation fileCopyOperationSecond = FileCopyOperation.builder()
        .sourcePath("")
        .destinationPath("gs://to-google-bucket/some-area/some-folder/filename.txt")
        .build();

    List<FileCopyOperationResponse> expectedList =
        Stream.of(
                FileCopyOperationResponse.builder()
                    .copyOperation(fileCopyOperationFirst)
                    .success(Boolean.FALSE)
                    .build(),
                FileCopyOperationResponse.builder()
                    .copyOperation(fileCopyOperationSecond)
                    .success(Boolean.FALSE)
                    .build())
            .collect(Collectors.toList());

    List<FileCopyOperationResponse> fileCopyOperationResponseList =
        storage.copyFiles(Stream.of(fileCopyOperationFirst, fileCopyOperationSecond)
            .collect(Collectors.toList()));

    assertEquals(expectedList, fileCopyOperationResponseList);
  }

  @Test
  void deleteFile() {
    String location = "gs://from-google-bucket/some-area/some-folder/filename.txt";
    BlobId sourceBlobId = Blob.newBuilder("from-google-bucket", "some-area/some-folder/filename.txt").build().getBlobId();
    Blob mockBlob = mock(Blob.class);
    when(googleCloudStorageUtil.getBucketName(any())).thenReturn("from-google-bucket");
    when(googleCloudStorageUtil.getDirectoryPath(any())).thenReturn("some-area/some-folder/filename.txt");
    when(tenantFactory.getTenantInfo(headers.getPartitionId())).thenReturn(tenantInfo);
    when(storageFactory.get(tenantInfo)).thenReturn(mockStorage);
    when(mockStorage.get("from-google-bucket", "some-area/some-folder/filename.txt")).thenReturn(mockBlob);
    when(mockStorage.delete(sourceBlobId)).thenReturn(true);
    when(mockBlob.getName()).thenReturn("some-area/some-folder/filename.txt");
    assertTrue(storage.deleteFile(location));
  }

  @Test
  void throwBadRequest() throws OsduBadRequestException {
    Assertions.assertThrows(OsduBadRequestException.class,
                            () -> storage.throwBadRequest("Error occurred"));
  }
}
