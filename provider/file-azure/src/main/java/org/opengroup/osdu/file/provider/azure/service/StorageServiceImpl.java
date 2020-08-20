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
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.azure.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements IStorageService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

  final FileLocationProperties fileLocationProperties;
  final IStorageRepository storageRepository;

  @Override
  public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
    log.debug("Creating the signed blob for fileID : {}. Authorization : {}, partitionID : {}",
        fileID, authorizationToken, partitionID);
    Instant now = Instant.now(Clock.systemUTC());

    String containerName = getContainerName(partitionID);

    String userDesID = getUserDesID(authorizationToken);
    String filepath = getFileLocationPrefix(now, fileID, userDesID);
    log.debug("Create storage object for fileID {} in container {} with filepath {}",
        fileID, containerName, filepath);

    if (filepath.length() > StorageConstant.AZURE_MAX_FILEPATH) {
      throw new BadRequestException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          StorageConstant.AZURE_MAX_FILEPATH, filepath.length()));
    }

    SignedObject signedObject = storageRepository.createSignedObject(containerName, filepath);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  private String getContainerName(String partitionID) {
    return partitionID;
  }

  private String getUserDesID(String authorizationToken) {
    return fileLocationProperties.getUserId();
  }

  private String getFileLocationPrefix(Instant instant, String filename, String userDesID) {
    return format("%s", filename);
  }

}
