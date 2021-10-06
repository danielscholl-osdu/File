/*
 * Copyright 2021 Google LLC
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

package org.opengroup.osdu.file.provider.reference.service;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.file.model.delivery.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.delivery.IDeliveryStorageService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryStorageServiceImpl implements IDeliveryStorageService {

  private final MinioStorageServiceImpl minioStorageService;

  @Override
  public SignedUrl createSignedUrl(String unsignedUrl, String authorizationToken) {
    String[] objectKeyParts = minioStorageService.getObjectKeyParts(unsignedUrl);
    String bucketName = objectKeyParts[0];
    String filePath =
        String.join("/", Arrays.copyOfRange(objectKeyParts, 1, objectKeyParts.length));
    return convertToDeliverySignedUrl(minioStorageService.createSignedUrl(bucketName, filePath));
  }

  private SignedUrl convertToDeliverySignedUrl(org.opengroup.osdu.file.model.SignedUrl signedUrl) {
    return SignedUrl.builder()
        .uri(signedUrl.getUri())
        .url(signedUrl.getUrl())
        .createdAt(signedUrl.getCreatedAt())
        .connectionString(signedUrl.getConnectionString())
        .build();
  }
}
