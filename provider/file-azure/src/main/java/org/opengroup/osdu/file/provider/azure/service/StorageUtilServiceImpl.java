/*
 * Copyright 2020  Microsoft Corporation
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

package org.opengroup.osdu.file.provider.azure.service;

import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobInputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.util.Strings;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.ChecksumAlgorithm;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.azure.util.FilePathUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Primary
public class StorageUtilServiceImpl implements IStorageUtilService  {
  private final String absolutePathFormat = "https://%s.blob.core.windows.net/%s/%s";

  @Autowired
  final BlobStoreConfig blobStoreConfig;

  @Autowired
  final FilePathUtil filePathUtil;

  @Autowired
  final BlobServiceClientWrapper blobServiceClientWrapper;

  @Autowired
  final BlobStore blobStore;

  @Autowired
  final ServiceHelper serviceHelper;

  @Autowired
  final DpsHeaders dpsHeaders;

  final JaxRsDpsLog log;

  @Override
  public String getPersistentLocation(String relativePath, String partitionId) {
    return String.format(
        absolutePathFormat,
        blobServiceClientWrapper.getStorageAccount(),
        blobStoreConfig.getPersistentContainer(),
        filePathUtil.normalizeFilePath(relativePath)
    );
  }

  @Override
  public String getStagingLocation(String relativePath, String partitionId) {
    return String.format(
        absolutePathFormat,
        blobServiceClientWrapper.getStorageAccount(),
        blobStoreConfig.getStagingContainer(),
        filePathUtil.normalizeFilePath(relativePath)
    );
  }

  @Override
  public String getChecksum(final String filePath) {
    if (Strings.isBlank(filePath)) {
      throw new OsduBadRequestException(String.format("Illegal file path argument - { %s }", filePath));
    }
    String sourceFilePath = serviceHelper.getRelativeFilePathFromAbsoluteFilePath(filePath);
    String containerName = serviceHelper.getContainerNameFromAbsoluteFilePath(filePath);
    try {
      BlobProperties blobProperties = blobStore.readBlobProperties(dpsHeaders.getPartitionId(), sourceFilePath, containerName);
      byte[] byteChecksum = blobProperties.getContentMd5();
      String fileID = sourceFilePath.split(StorageConstant.SLASH)[2];
      if (byteChecksum != null && byteChecksum.length > 0) {
        log.info("checksum is available for fileId "+fileID);
        return new String(Hex.encodeHex(byteChecksum));
      } else {
        log.info("checksum is not available, calculating the checksum for fileId "+fileID);
        return calculateChecksum(sourceFilePath, containerName);
      }
    } catch (BlobStorageException ex) {
      throw new OsduBadRequestException(FileMetadataConstant.METADATA_EXCEPTION + filePath, ex);
    }
  }

  private String calculateChecksum(String filePath, String containerName) {
    try {
      MessageDigest md = MessageDigest.getInstance(getChecksumAlgorithm().toString());
      BlobInputStream blobInputStream = blobStore.getBlobInputStream(dpsHeaders.getPartitionId(), filePath, containerName);
      byte[] bytes = new byte[StorageConstant.AZURE_MAX_FILEPATH];
      int numBytes;
      while ((numBytes = blobInputStream.read(bytes)) != -1) {
        md.update(bytes, 0, numBytes);
      }
      byte[] digest = md.digest();
      return new String(Hex.encodeHex(digest));
    } catch (NoSuchAlgorithmException ex) {
      String message = FileMetadataConstant.CHECKSUM_EXCEPTION + filePath;
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, message , ex.getMessage(), ex);
    } catch (IOException ex) {
      String message = FileMetadataConstant.CHECKSUM_EXCEPTION + filePath;
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, message , ex.getMessage(), ex);
    }
  }

  @Override
  public ChecksumAlgorithm getChecksumAlgorithm() { return ChecksumAlgorithm.MD5; }
}
