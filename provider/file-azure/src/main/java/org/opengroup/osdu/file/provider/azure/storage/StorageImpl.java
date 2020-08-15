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


import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.file.provider.azure.common.base.MoreObjects;
import org.opengroup.osdu.file.provider.azure.service.AzureTokenServiceImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StorageImpl implements Storage {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private static String clientSecret = System.getProperty("AZURE_CLIENT_SECRET", System.getenv("TESTER_SERVICEPRINCIPAL_SECRET"));
  private static String clientId = System.getProperty("AZURE_CLIENT_ID", System.getenv("INTEGRATION_TESTER"));
  private static String tenantId = System.getProperty("AZURE_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
  private static String storageAccount;

  @Inject
  AzureTokenServiceImpl token;

  public StorageImpl() {
    this.storageAccount = getStorageAccount();
  }

  @Override
  public Blob create(BlobInfo blobInfo, byte[] content) {
    content = (byte[]) MoreObjects.firstNonNull(content, EMPTY_BYTE_ARRAY);
    log.debug("Creating the blob in container {} for path {}", blobInfo.getContainer(), blobInfo.getName());
    return this.internalCreate(blobInfo, content);
  }

  @SneakyThrows
  private Blob internalCreate(BlobInfo info, final byte[] content) {
    String blobPath = generateBlobPath(storageAccount, info.getContainer(), info.getName());
    BlobUrlParts parts = BlobUrlParts.parse(blobPath);
    BlobContainerClient blobContainerClient = getBlobContainerClient(parts.getAccountName(), parts.getBlobContainerName());
    if (!blobContainerClient.exists()) {
      createContainer(parts.getBlobContainerName());
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
    return null;
  }


  @SneakyThrows
  @Override
  public URL signUrl(BlobInfo blobInfo, long duration, TimeUnit timeUnit) {
    URL url = null;
    try {
      log.debug("Signing the blob in container {} for path {}", blobInfo.getContainer(), blobInfo.getName());
      System.out.println(String.format("Signing the blob in container %s for path %s", blobInfo.getContainer(), blobInfo.getName()));
      String blobURL = generateBlobPath(storageAccount, blobInfo.getContainer(), blobInfo.getName());
      System.out.println(String.format("Signing the blob %s", blobURL));
      log.debug("Signing the blob {}", blobURL);
      String signedUrl = token.sign(blobURL, duration, timeUnit);
      System.out.println(String.format("signedUrl: %s", signedUrl));
      return new URL(signedUrl);
    }
    catch (MalformedURLException e) {
      throw e;
    }
  }

  public static String getStorageAccount() {
    return System.getProperty("AZURE_STORAGE_ACCOUNT", System.getenv("AZURE_STORAGE_ACCOUNT"));
  }

  private static String generateContainerPath(String accountName, String containerName) {
    return String.format("https://%s.blob.core.windows.net/%s", accountName, containerName);
  }

  public void createContainer(String containerName)
  {
    String containerPath = generateContainerPath(storageAccount, containerName);
    BlobUrlParts parts = BlobUrlParts.parse(containerPath);
    BlobContainerClient blobContainerClient = getBlobContainerClient(parts.getAccountName(), parts.getBlobContainerName());
    if(!blobContainerClient.exists()){
      blobContainerClient.create();
      log.debug("Created the container {}", containerName);
    }
  }

  private BlobContainerClient getBlobContainerClient(String accountName, String containerName) {
    ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
        .clientSecret(clientSecret)
        .clientId(clientId)
        .tenantId(tenantId)
        .build();
    BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
        .endpoint(getBlobAccountUrl(accountName))
        .credential(clientSecretCredential)
        .containerName(containerName)
        .buildClient();
    return blobContainerClient;
  }

  private static String getBlobAccountUrl(String accountName) {
    return String.format("https://%s.blob.core.windows.net", accountName);
  }
  private static String generateBlobPath(String accountName, String containerName, String blobName) {
    return String.format("https://%s.blob.core.windows.net/%s/%s", accountName, containerName, blobName);
  }

}

