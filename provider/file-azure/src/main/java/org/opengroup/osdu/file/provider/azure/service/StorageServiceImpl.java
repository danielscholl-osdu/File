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

import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.exception.OsduUnauthorizedException;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.azure.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Service
@Slf4j
@AllArgsConstructor
public class StorageServiceImpl implements IStorageService {
  @Autowired
  BlobStore blobStore;

  private static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

  @Autowired
  DpsHeaders dpsHeaders;

  @Autowired
  final FileLocationProperties fileLocationProperties;
  @Autowired
  final IStorageRepository storageRepository;

  @Autowired
  final BlobStoreConfig blobStoreConfig;

  @Autowired
  ServiceHelper serviceHelper;

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
        .fileSource(getRelativeFileSource(filepath))
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  private String getRelativeFileSource(String filePath) {
    return "/" + filePath;
  }

  private String getContainerName(String partitionID) {
    return blobStoreConfig.getStagingContainer();
  }

  private String getUserDesID(String authorizationToken) {
    return fileLocationProperties.getUserId();
  }

  private String getFileLocationPrefix(Instant instant, String filename, String userDesID) {
    String folderName = instant.toEpochMilli() + "-"
        + DATE_TIME_FORMATTER
        .withZone(ZoneOffset.UTC)
        .format(instant);

    return format("%s/%s/%s", userDesID, folderName, filename);
  }

  @SneakyThrows
  @Override
  public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken) {
    if(StringUtils.isBlank(authorizationToken) || StringUtils.isBlank(unsignedUrl)) {
      throw new IllegalArgumentException(
          String.format("invalid received for authorizationToken (value: %s) or unsignedURL (value: %s)",
              authorizationToken, unsignedUrl));
    }

    String containerName = serviceHelper.getContainerNameFromAbsoluteFilePath(unsignedUrl);
    String filePath = serviceHelper.getRelativeFilePathFromAbsoluteFilePath(unsignedUrl);
    BlobSasPermission permission = new BlobSasPermission();
    permission.setReadPermission(true);
    OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7);

    String signedUrlString = blobStore.generatePreSignedURL(
        dpsHeaders.getPartitionId(),
        filePath.toString(),
        containerName,
        expiryTime,
        permission);

    if(StringUtils.isBlank(signedUrlString)) {
      throw new InternalServerErrorException(String.format("Could not generate signed URL for file location %s", unsignedUrl));
    }

    return SignedUrl.builder()
          .url(new URL(signedUrlString))
          .uri(URI.create(unsignedUrl))
          .createdBy(getUserDesID(authorizationToken))
          .createdAt(Instant.now(Clock.systemUTC()))
          .build();
  }
}
