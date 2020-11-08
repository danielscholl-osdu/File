package org.opengroup.osdu.file.provider.gcp.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.multitenancy.GcsMultiTenantAccess;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleCloudStorageOperationImpl implements ICloudStorageOperation {

  private static final String INVALID_RESOURCE_PATH = "Storage record does not have a valid file url";

  final DpsHeaders headers;
  final GcsMultiTenantAccess storageFactory;
  final TenantFactory tenantFactory;
  final GoogleCloudStorageUtil googleCloudStorageUtil;
  final JaxRsDpsLog log;


  @Override
  public String copyFile(String sourceFilePath, String toFile) throws OsduBadRequestException {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(headers.getPartitionId());
    Storage storage = storageFactory.get(tenantInfo);

    String fromBucket = googleCloudStorageUtil.getBucketName(sourceFilePath);
    String fromPath = googleCloudStorageUtil.getDirectoryPath(sourceFilePath);
    String destinationBucket = googleCloudStorageUtil.getBucketName(toFile);
    String destinationFilePath = googleCloudStorageUtil.getDirectoryPath(toFile);

    if (googleCloudStorageUtil.isPathsEmpty(fromBucket,
                                            fromPath,
                                            destinationBucket,
                                            destinationFilePath)) {
      throwBadRequest(INVALID_RESOURCE_PATH);
    }

    Blob sourceBlob = storage.get(fromBucket, fromPath);
    if (sourceBlob == null) {
      throwBadRequest(getErrorMessageFileNotPresent(fromPath),
                      FileMetadataConstant.INVALID_SOURCE_EXCEPTION + sourceFilePath);
    }
    String fileContentType = sourceBlob.getContentType();
    BlobId sourceBlobId = sourceBlob.getBlobId();
    BlobId targetBlobId = BlobId.of(destinationBucket, destinationFilePath);
    List<Acl> finalAcls = Stream
        .concat(googleCloudStorageUtil.getAcls(tenantInfo.getServiceAccount()).stream(),
                storage.listAcls(sourceBlobId).stream())
        .collect(Collectors.toList());
    BlobInfo targetBlobInfo = BlobInfo
        .newBuilder(targetBlobId)
        .setContentType(fileContentType)
        .setAcl(finalAcls)
        .build();

    Storage.CopyRequest copyRequest = Storage.CopyRequest.of(sourceBlobId, targetBlobInfo);
    CopyWriter copyWriter = storage.copy(copyRequest);
    return googleCloudStorageUtil.getCompleteFilePath(destinationBucket,
                                                      copyWriter.getResult().getName());
  }

  @Override
  public Boolean deleteFile(String location) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(headers.getPartitionId());
    Storage storage = storageFactory.get(tenantInfo);
    String bucketName = googleCloudStorageUtil.getBucketName(location);
    String filePath = googleCloudStorageUtil.getDirectoryPath(location);
    String folderName = googleCloudStorageUtil.getFolderName(filePath);
    Boolean isFileDeleted = deleteStorageBlob(storage, bucketName, filePath);
    if (isFileDeleted && isDirEmpty(storage, bucketName, folderName)) {
      deleteStorageBlob(storage, bucketName, folderName);
    }
    return isFileDeleted;
  }

  private Boolean deleteStorageBlob(Storage storage, String bucketName, String filePath) {
    Blob blob = storage.get(bucketName, filePath);
    Boolean isDeleted = false;
    if (blob != null) {
      String sourceBlobName = blob.getName();
      BlobId sourceBlobId = BlobId.of(bucketName, sourceBlobName);
      isDeleted = storage.delete(sourceBlobId);
    }
    return isDeleted;
  }

  private Boolean isDirEmpty(Storage storage, String bucket, String folderName) {
    Page<Blob> blobs = readStorage(storage, bucket, folderName);
    if (blobs == null) {
      return true;
    }
    for (Blob blob : blobs.iterateAll()) {
      if (!blob.getName().equals(folderName)) {
        return false;
      }
    }
    return true;
  }

  private Page<Blob> readStorage(Storage storage, String bucket, String location) {
    return storage.list(bucket,
                        Storage.BlobListOption.currentDirectory(),
                        Storage.BlobListOption.prefix(location));

  }

  void throwBadRequest(String errorMessage, String errorMessageLog) throws OsduBadRequestException {
    OsduBadRequestException ex = new OsduBadRequestException(errorMessage);
    log.error(errorMessageLog != null ? errorMessageLog : errorMessage, ex);
    throw ex;
  }

  void throwBadRequest(String errorMessage) throws OsduBadRequestException {
    throwBadRequest(errorMessage, null);
  }

  private String getErrorMessageFileNotPresent(String fromPath) {
    return FileMetadataConstant.INVALID_SOURCE_EXCEPTION + FileMetadataConstant.FORWARD_SLASH
        + fromPath;
  }

}
