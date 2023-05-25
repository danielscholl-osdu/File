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
import java.util.Map;

import javax.inject.Inject;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.di.MSIConfiguration;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.model.blob.Blob;
import org.opengroup.osdu.file.provider.azure.model.blob.BlobId;
import org.opengroup.osdu.file.provider.azure.model.blob.BlobInfo;
import org.opengroup.osdu.file.provider.azure.storage.Storage;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
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
  private MSIConfiguration msiConfiguration;

  @Autowired
  DpsHeaders dpsHeaders;

  @Autowired
  private ExpiryTimeUtil expiryTimeUtil;

  @Override
  @SneakyThrows
  public SignedObject createSignedObject(String containerName, String filepath) {
    return getSignedObjectBasedOnParams(containerName, filepath, new SignedUrlParameters());
  }

  @Override
  @SneakyThrows
  public SignedObject getSignedObjectBasedOnParams(String containerName, String filepath,
                                                   SignedUrlParameters signedUrlParameters) {
    log.debug("Creating the signed blob in container {} for path {}", containerName, filepath);
    BlobId blobId = BlobId.of(containerName, filepath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    Blob blob = storage.create(dpsHeaders.getPartitionId(), blobInfo, ArrayUtils.EMPTY_BYTE_ARRAY);
    log.debug("Created the blob in container {} for path {}", containerName, filepath);

    OffsetDateTime expiryTime = expiryTimeUtil.getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime());
    BlobSasPermission permissions = (new BlobSasPermission())
        .setWritePermission(true)
        .setCreatePermission(true);

    String signedUrlStr;
    if (!msiConfiguration.getIsEnabled()) {
      signedUrlStr = blobStore.generatePreSignedURL(dpsHeaders.getPartitionId(), filepath, containerName, expiryTime, permissions);
    } else {
      signedUrlStr = blobStore.generatePreSignedUrlWithUserDelegationSas(dpsHeaders.getPartitionId(), containerName, filepath, expiryTime, permissions);
    }

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

  @Override
  @SneakyThrows
  public Boolean revokeUserDelegationKeys(Map<String, String> revokeURLRequest) {
    AzureResourceManager azureResourceManager = azureResourceManager();
    String resourceGroupName = revokeURLRequest.get("resourceGroup");
    String storageAccountName = revokeURLRequest.get("storageAccount");
    log.debug("Revoke the signed urls for the storage account {} in Resource group {}", storageAccountName, resourceGroupName);
    try {
      azureResourceManager
          .storageAccounts()
          .manager()
          .serviceClient()
          .getStorageAccounts()
          .revokeUserDelegationKeysWithResponse(resourceGroupName, storageAccountName, Context.NONE);
      log.debug("Revoked the signed urls for the storage account {} in Resource group {}", storageAccountName, resourceGroupName);
    } catch (Exception ex) {
      String message = "Error occurred while revoking signed urls";
      log.error(message + ex.getMessage(), ex);
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, message , ex.getMessage(), ex);
    }
    return true;
  }

  private static AzureResourceManager azureResourceManager() {
    AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
    AzureResourceManager azureResourceManager = AzureResourceManager
        .authenticate(tokenCredential(), azureProfile)
        .withSubscription(azureProfile.getSubscriptionId());
    return azureResourceManager;
  }

  private static ClientSecretCredential tokenCredential() {
    Configuration configuration = Configuration.getGlobalConfiguration();
    return new ClientSecretCredentialBuilder()
        .clientId(configuration.get("AZURE_CLIENT_ID"))
        .clientSecret(configuration.get("AZURE_CLIENT_SECRET"))
        .tenantId(configuration.get("AZURE_TENANT_ID"))
        .build();
  }
}
