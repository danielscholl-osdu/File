/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.provider.gcp.service;

import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.gcp.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.gcp.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.gcp.util.GoogleCloudStorageUtil;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleCloudStorageServiceImpl implements IStorageService {

  private final static String INVALID_GS_PATH_REASON = "Unsigned url invalid, needs to be full GS path";

  final FileLocationProperties fileLocationProperties;
  final IStorageRepository storageRepository;

  @Autowired
  GoogleCloudStorageUtil googleCloudStorageUtil;

  @Autowired
  TenantFactory tenantFactory;

  @Autowired
  Storage storage;

  @Override
  public SignedUrl createSignedUrl(String fileName, String authorizationToken, String partitionID) {
    log.debug("Creating the signed blob for fileName : {}. Authorization : {}, partitionID : {}",
              fileName, authorizationToken, partitionID);

    TenantInfo tenantInfo = tenantFactory.getTenantInfo(partitionID);
    Instant now = Instant.now(Clock.systemUTC());

    String filepath = getRelativePath(fileName);
    String bucketName = googleCloudStorageUtil.getStagingBucket(tenantInfo.getProjectId());
    String userDesID = getUserDesID(authorizationToken);
    log.debug("Create storage object for fileName {} in bucket {} with filepath {}",
              fileName, bucketName, filepath);

    if (filepath.length() > StorageConstant.GCS_MAX_FILEPATH) {
      throw new OsduBadRequestException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          StorageConstant.GCS_MAX_FILEPATH, filepath.length()));
    }

    SignedObject signedObject = storageRepository.createSignedObject(bucketName, filepath);

    return SignedUrl.builder()
                    .url(signedObject.getUrl())
                    .uri(signedObject.getUri())
                    .fileSource(getRelativeFileSource(filepath))
                    .createdBy(userDesID)
                    .createdAt(now)
                    .build();
  }


  @Override
  public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken) {
    Instant now = Instant.now(Clock.systemUTC());

    String[] gsPathParts = unsignedUrl.split("gs://");

    if (gsPathParts.length < 2) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL", INVALID_GS_PATH_REASON);
    }

    String[] gsObjectKeyParts = gsPathParts[1].split("/");
    if (gsObjectKeyParts.length < 1) {
      throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed URL", INVALID_GS_PATH_REASON);
    }

    String bucketName = gsObjectKeyParts[0];
    String filePath = String.join("/", Arrays.copyOfRange(gsObjectKeyParts, 1, gsObjectKeyParts.length));

    SignedObject signedObject = storageRepository.getSignedObject(bucketName, filePath);


    return SignedUrl.builder()
                    .url(signedObject.getUrl())
                    .uri(signedObject.getUri())
                    .createdAt(now)
                    .build();

  }

  private String getRelativeFileSource(String filepath) {
    return "/" + filepath;
  }

  private String getUserDesID(String authorizationToken) {
    return fileLocationProperties.getUserId();
  }

  private String getRelativePath(String filename) {
    String folderName = UUID.randomUUID().toString();

    return format("%s/%s", folderName, filename);
  }

}
