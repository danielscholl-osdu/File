package org.opengroup.osdu.file.provider.reference.service;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.ListObjectsArgs;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.opengroup.osdu.file.provider.reference.repository.MinioRepository;
import org.opengroup.osdu.file.provider.reference.util.MinioPathUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceStorageOperationImpl implements ICloudStorageOperation {

  private static final String INVALID_RESOURCE_PATH = "Storage record does not have a valid file url";

  private final MinioPathUtil minioPathUtil;
  private final MinioRepository minioRepository;

  @Override
  public String copyFile(String sourceFilePath, String toFile) throws OsduBadRequestException {
    String fromBucket = minioPathUtil.getBucketName(sourceFilePath);
    String fromPath = minioPathUtil.getDirectoryPath(sourceFilePath);
    String destinationBucket = minioPathUtil.getBucketName(toFile);
    String destinationFilePath = minioPathUtil.getDirectoryPath(toFile);
    validatePaths(fromBucket, fromPath, destinationBucket, destinationFilePath);

    CopyObjectArgs copyArgs = CopyObjectArgs.builder()
        .source(CopySource.builder()
            .bucket(fromBucket)
            .object(fromPath)
            .build())
        .bucket(destinationBucket)
        .object(destinationFilePath)
        .build();
    ObjectWriteResponse writeResponse = minioRepository.copyFile(copyArgs);
    return minioPathUtil.getCompleteFilePath(destinationBucket, writeResponse.object());
  }

  private void validatePaths(String fromBucket, String fromPath, String destinationBucket,
      String destinationFilePath) {
    if (Stream.of(fromBucket, fromPath, destinationBucket, destinationFilePath)
        .anyMatch(StringUtils::isEmpty)) {
      log.error(INVALID_RESOURCE_PATH);
      throw new OsduBadRequestException(INVALID_RESOURCE_PATH);
    }
  }

  @Override
  public List<FileCopyOperationResponse> copyFiles(List<FileCopyOperation> fileCopyOperationList) {
    return fileCopyOperationList.stream()
        .map(operation -> {
          try {
            this.copyFile(operation.getSourcePath(), operation.getDestinationPath());
            return FileCopyOperationResponse.builder()
                .copyOperation(operation)
                .success(Boolean.TRUE)
                .build();
          } catch (OsduBadRequestException e) {
            log.error("Error in performing file copy operation", e);
            return FileCopyOperationResponse.builder()
                .copyOperation(operation)
                .success(Boolean.FALSE)
                .build();
          }
        }).collect(Collectors.toList());
  }

  @Override
  public Boolean deleteFile(String location) {
    String bucketName = minioPathUtil.getBucketName(location);
    String filePath = minioPathUtil.getDirectoryPath(location);
    String folderName = minioPathUtil.getFolderName(filePath);
    Boolean isFileDeleted = deleteBlob(bucketName, filePath);
    if (isFileDeleted && isDirEmpty(bucketName, folderName)) {
      deleteBlob(bucketName, folderName);
    }
    return isFileDeleted;
  }

  private Boolean deleteBlob(String bucketName, String filePath) {
    RemoveObjectArgs deleteArgs = RemoveObjectArgs.builder()
        .bucket(bucketName)
        .object(filePath)
        .build();
    minioRepository.deleteFile(deleteArgs);
    return true;
  }

  // TODO: handle result.get exception
  private Boolean isDirEmpty(String bucket, String folderName) {
    ListObjectsArgs listObjectsArgs =
        ListObjectsArgs.builder().bucket(bucket).recursive(true).build();
    Iterable<Result<Item>> results = minioRepository.listObjects(listObjectsArgs);
    return results == null
        || StreamSupport.stream(results.spliterator(), false)
            .allMatch(
                result -> {
                  try {
                    return result.get().objectName().equals(folderName);
                  } catch (Exception e) {
                    throw new AppException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Failed to get list of objects.",
                        e.getMessage());
                  }
                });
  }
}
