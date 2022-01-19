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

import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.time.OffsetDateTime;

@Repository("DataLake")
@Slf4j
public class DataLakeRepository implements IStorageRepository {

  @Autowired
  DataLakeStore dataLakeStore;

  @Autowired
  DpsHeaders dpsHeaders;

  /**
   * Creates the empty object blob in bucket by filepath.
   *
   * @param directoryName file path
   * @return info created blob and signed URL
   */
  @Override
  @SneakyThrows
  public SignedObject createSignedObject(String containerName, String directoryName) {
    log.info("Creating the directory in FileSystem {} for path {}", containerName, directoryName);
    dataLakeStore.createDirectory(dpsHeaders.getPartitionId(), containerName, directoryName);
    log.debug("Created the directory in FileSystem {}", containerName);

    int expiryDays = 7;
    OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(expiryDays);

    FileSystemSasPermission permission = new FileSystemSasPermission().setReadPermission(true)
        .setAddPermission(true)
        .setWritePermission(true)
        .setCreatePermission(true)
        .setListPermission(true);

    String signedUrlStr = dataLakeStore.generatePreSignedURL(dpsHeaders.getPartitionId(),
        containerName, directoryName, expiryTime, permission);

    URL signedUrl = new URL(signedUrlStr);
    return SignedObject.builder()
        .url(signedUrl)
        .build();
  }
}
