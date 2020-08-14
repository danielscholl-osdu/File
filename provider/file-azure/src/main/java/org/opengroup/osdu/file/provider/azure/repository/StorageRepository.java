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
import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.azure.storage.Blob;
import org.opengroup.osdu.file.provider.azure.storage.BlobId;
import org.opengroup.osdu.file.provider.azure.storage.BlobInfo;
import org.opengroup.osdu.file.provider.azure.storage.Storage;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriUtils;

import javax.inject.Inject;

@Repository
@Slf4j
public class StorageRepository implements IStorageRepository {

  @Inject
  final Storage storage;

  private static String storageAccount;

  public StorageRepository(Storage storage) {
    this.storage = storage;
    this.storageAccount = getStorageAccount();
  }
  @Override
  public SignedObject createSignedObject(String containerName, String filepath) {
    log.debug("Creating the signed blob in container {} for path {}", containerName, filepath);
    System.out.println(String.format("Creating the signed blob in container %s for path %s", containerName, filepath));
    BlobId blobId = BlobId.of(containerName, filepath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    Blob blob = storage.create(blobInfo, ArrayUtils.EMPTY_BYTE_ARRAY);
    log.debug("Created the  blob in container {} for path {}", containerName, filepath);
    System.out.println(String.format("Created the  blob in container %s for path %s", containerName, filepath));
    URL signedUrl = storage.signUrl(blobInfo, 7L, TimeUnit.DAYS);
    log.debug("Signed URL for created storage object. Object ID : {} , Signed URL : {}",
        blob.getGeneratedId(), signedUrl);
    return SignedObject.builder()
        .uri(getObjectUri(blob))
        .url(signedUrl)
        .build();
  }

  public static String getStorageAccount() {
    return System.getProperty("AZURE_STORAGE_ACCOUNT", System.getenv("AZURE_STORAGE_ACCOUNT"));
  }

  private URI getObjectUri(Blob blob) {
    String filepath = UriUtils.encodePath(blob.getName(), StandardCharsets.UTF_8);
    return URI.create(format("%s%s.blob.core.windows.net/%s/%s", AZURE_PROTOCOL, storageAccount, blob.getContainer(), filepath));
  }
}
