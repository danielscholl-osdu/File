// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.file.provider.aws.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.aws.di.IFileLocationProvider;
import org.opengroup.osdu.file.provider.aws.model.FileDmsUploadLocation;
import org.opengroup.osdu.file.provider.aws.model.FileUploadLocation;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.net.MalformedURLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequestScope
public class StorageServiceImpl implements IStorageService {

    private final IFileLocationProvider fileLocationProvider;
    private final DpsHeaders headers;
    private final ObjectMapper objectMapper;

    @Autowired
    public StorageServiceImpl(IFileLocationProvider fileLocationProvider,
                              DpsHeaders headers,
                              ObjectMapper objectMapper,
                              @Value("${PROVIDER_KEY}") String PROVIDER_KEY) {
        this.fileLocationProvider = fileLocationProvider;
        this.headers = headers;
        this.objectMapper = objectMapper;
    }

    @Override
    public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
        log.debug("Creating the signed URL for file ID: {}, authorization: {}, partition ID: {}", fileID, authorizationToken, partitionID);

        final FileUploadLocation fileUploadLocation = fileLocationProvider.getUploadLocation(fileID, partitionID);

        String userEmail = this.headers.getUserEmail();
        Instant now = Instant.now(Clock.systemUTC());

        try {
            return SignedUrl.builder()
                            .uri(fileUploadLocation.getSignedUrl())
                            .url(fileUploadLocation.getSignedUrl().toURL())
                            .fileSource(fileUploadLocation.getUnsignedUrl() + fileUploadLocation.getSignedUploadFileName())
                            .connectionString(fileUploadLocation.getConnectionString())
                            .createdBy(userEmail)
                            .createdAt(now)
                            .build();
        } catch (MalformedURLException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Failed to parse URI into URL for File Signed Url Path");
        }
    }

    @Override
    public StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionID) {
        SignedUrl signedUrl = this.createSignedUrl(datasetId, headers.getAuthorization(), partitionID);

        FileDmsUploadLocation dmsLocation = FileDmsUploadLocation.builder()
                                                                 .signedUrl(signedUrl.getUrl().toString())
                                                                 .createdBy(signedUrl.getCreatedBy())
                                                                 .fileSource(signedUrl.getFileSource()).build();

        Map<String, Object> uploadLocation = objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

        return StorageInstructionsResponse.builder()
                                          .providerKey(environmentResolver.getProviderKey())
                                          .storageLocation(uploadLocation)
                                          .build();
    }

    @Override
    public SignedUrl createSignedUrlFileLocation(String unsignedUrl,
                                                 String authorizationToken,
                                                 SignedUrlParameters signedUrlParameters) {
        throw new NotImplementedException("Not implemented. Use createSignedUrl(fileId, ...) instead");
    }
}
