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

package org.opengroup.osdu.file.provider.azure.repository;

import static java.lang.String.format;
import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.AZURE_PROTOCOL;
import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.BLOB_RESOURCE_BASE_URI_REGEX;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.storage.Blob;
import org.opengroup.osdu.file.provider.azure.storage.BlobId;
import org.opengroup.osdu.file.provider.azure.storage.BlobInfo;
import org.opengroup.osdu.file.provider.azure.storage.Storage;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriUtils;

import com.azure.storage.blob.sas.BlobSasPermission;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StorageRepository implements IStorageRepository {

  @Inject
  private Storage storage;

  @Autowired
  BlobStore blobStore;

  @Autowired
  BlobStoreConfig blobStoreConfig;

  @Autowired
  private BlobServiceClientWrapper blobServiceClientWrapper;

  @Autowired
  DpsHeaders dpsHeaders;

  @Override
  @SneakyThrows
  public SignedObject createSignedObject(String containerName, String filepath) {
    log.debug("Creating the signed blob in container {} for path {}", containerName, filepath);
    BlobId blobId = BlobId.of(containerName, filepath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    Blob blob = storage.create(dpsHeaders.getPartitionId(), blobInfo, ArrayUtils.EMPTY_BYTE_ARRAY);
    log.debug("Created the blob in container {} for path {}", containerName, filepath);

    int expiryDays = 7;
    OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);
    BlobSasPermission permissions = (new BlobSasPermission())
        .setWritePermission(true)
        .setCreatePermission(true);
    String signedUrlStr =  blobStore.generatePreSignedURL(dpsHeaders.getPartitionId(), filepath, containerName, expiryTime, permissions);

    URL signedUrl = new URL(signedUrlStr);
    log.debug("Signed URL for created storage object. Object ID : {} , Signed URL : {}",
        blob.getGeneratedId(), signedUrl);
    return SignedObject.builder()
        .uri(getObjectUri(blob))
        .url(signedUrl)
        .build();
  }

  private String getStorageAccountEndpoint(Blob blob) {
    String filepath = UriUtils.encodePath(blob.getName(), StandardCharsets.UTF_8);
    return format(BLOB_RESOURCE_BASE_URI_REGEX, AZURE_PROTOCOL, getStorageAccount(), blob.getContainer(), filepath);
  }

  private URI getObjectUri(Blob blob) {
    return URI.create(getStorageAccountEndpoint(blob));
  }

  private String getStorageAccount() {
    return blobServiceClientWrapper.getStorageAccount();
  }
}
