/*
 * Copyright 2021 Google LLC
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

package org.opengroup.osdu.file.provider.reference.repository;

import io.minio.CopyObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.provider.reference.factory.CloudObjectStorageFactory;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MinioRepositoryImpl implements MinioRepository {

  private final CloudObjectStorageFactory storageFactory;
  private MinioClient minioClient;

  @PostConstruct
  public void init() {
    minioClient = storageFactory.getClient();
  }

  @Override
  public InputStream getFile(GetObjectArgs args) {
    log.debug("Reading the object with args: {}", args);
    try {
      return minioClient.getObject(args);
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to read file.", e.getMessage());
    }
  }

  @Override
  public Iterable<Result<Item>> listObjects(ListObjectsArgs args) {
    log.debug("Getting the list of object with args: {}", args);
    try {
      return minioClient.listObjects(args);
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to get list of objects.", e.getMessage());
    }
  }

  @Override
  public String getSignedUrl(GetPresignedObjectUrlArgs args) {
    log.debug("Creating the signed blob with args: {}", args);
    try {
      return minioClient.getPresignedObjectUrl(args);
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to get singed url.", e.getMessage());
    }
  }

  @Override
  public ObjectWriteResponse copyFile(CopyObjectArgs args) {
    log.debug("Copying the object with args: {}", args);
    try {
      return minioClient.copyObject(args);
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to copy file.", e.getMessage());
    }
  }

  @Override
  public void deleteFile(RemoveObjectArgs args) {
    log.debug("Deleting the file with args: {}", args);
    try {
      minioClient.removeObject(args);
    } catch (Exception e) {
      throw new AppException(
          HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed to delete file.", e.getMessage());
    }
  }
}
