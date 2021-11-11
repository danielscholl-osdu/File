// Copyright © Microsoft Corporation
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

package org.opengroup.osdu.file.provider.azure.storage;


import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.opengroup.osdu.azure.blobstorage.IBlobContainerClientFactory;
import org.opengroup.osdu.file.provider.azure.common.base.MoreObjects;
import org.opengroup.osdu.file.provider.azure.config.AzureBootstrapConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.service.AzureTokenServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StorageImpl implements Storage {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  @Autowired
  private AzureBootstrapConfig azureBootstrapConfig;

  @Autowired
  private BlobServiceClientWrapper blobServiceClientWrapper;

  @Autowired
  private IBlobContainerClientFactory blobContainerClientFactory;

  @Inject
  AzureTokenServiceImpl token;

  @Override
  public Blob create(String dataPartitionId, BlobInfo blobInfo, byte[] content) {
    content = (byte[]) MoreObjects.firstNonNull(content, EMPTY_BYTE_ARRAY);
    log.debug("Creating the blob in container {} for path {}", blobInfo.getContainer(), blobInfo.getName());
    return this.internalCreate(dataPartitionId, blobInfo, content);
  }

  @SneakyThrows
  private Blob internalCreate(String dataPartitionId, BlobInfo info, final byte[] content) {
    String blobPath = generateBlobPath(blobServiceClientWrapper.getStorageAccount(), info.getContainer(), info.getName());
    BlobUrlParts parts = BlobUrlParts.parse(blobPath);
    BlobContainerClient blobContainerClient = blobContainerClientFactory.getClient(dataPartitionId, parts.getBlobContainerName());
    if (!blobContainerClient.exists()) {
      blobContainerClient.create();
      log.debug("Created the container {}", parts.getBlobContainerName());
    }
    BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(parts.getBlobName()).getBlockBlobClient();
    if (!blockBlobClient.exists()) {
      try (ByteArrayInputStream dataStream = new ByteArrayInputStream(content)) {
        blockBlobClient.upload(dataStream, content.length);
        log.debug("Created the blob in container {} for path {}", info.getContainer(), info.getName());
        return new Blob(this, new BlobInfo.BuilderImpl(info.getBlobId()));
      } catch (Exception e) {
        throw e;
      }
    }
    return new Blob(this, new BlobInfo.BuilderImpl(info.getBlobId()));
  }

  @SneakyThrows
  @Override
  public URL signUrl(BlobInfo blobInfo, long duration, TimeUnit timeUnit) {
    try {
      log.debug("Signing the blob in container {} for path {}", blobInfo.getContainer(), blobInfo.getName());
      String blobURL = generateBlobPath(blobServiceClientWrapper.getStorageAccount(), blobInfo.getContainer(), blobInfo.getName());
      log.debug("Signing the blob {}", blobURL);
      String signedUrl = token.sign(blobURL, duration, timeUnit);
      return new URL(signedUrl);
    }
    catch (MalformedURLException e) {
      throw e;
    }
  }

  private static String generateBlobPath(String accountName, String containerName, String blobName) {
    return String.format("https://%s.blob.core.windows.net/%s/%s", accountName, containerName, blobName);
  }

}

