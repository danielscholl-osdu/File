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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.util.FilePathUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@Slf4j
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
}
