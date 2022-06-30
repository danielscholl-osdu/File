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

package org.opengroup.osdu.file.provider.aws.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.aws.helper.S3Helper;
import org.opengroup.osdu.file.provider.aws.model.FileCollectionDmsStorageLocation;
import org.opengroup.osdu.file.provider.aws.model.ProviderLocation;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.service.FileLocationProvider;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.opengroup.osdu.file.util.ExpiryTimeUtil.RelativeTimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequestScope
public class FileCollectionStorageServiceImpl implements IFileCollectionStorageService {

    private final FileLocationProvider fileLocationProvider;
    private final DpsHeaders headers;
    private final ObjectMapper objectMapper;
    private final ExpiryTimeUtil expiryTimeUtil;

    @Autowired
    public FileCollectionStorageServiceImpl(FileLocationProvider fileLocationProvider, DpsHeaders headers, ObjectMapper objectMapper,
                                            ExpiryTimeUtil expiryTimeUtil) {
        this.fileLocationProvider = fileLocationProvider;
        this.headers = headers;
        this.objectMapper = objectMapper;
        this.expiryTimeUtil = expiryTimeUtil;
    }

    @Override
    public StorageInstructionsResponse createStorageInstructions(String datasetID, String partitionID) {
        final ProviderLocation fileLocation = fileLocationProvider.getFileCollectionUploadLocation(datasetID, partitionID);
        final S3Location unsignedLocation = S3Location.of(fileLocation.getUnsignedUrl());

        final FileCollectionDmsStorageLocation dmsLocation = getFileCollectionDmsStorageLocation(fileLocation, unsignedLocation, this.headers);
        final Map<String, Object> uploadLocation = objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

        return StorageInstructionsResponse.builder()
            .storageLocation(uploadLocation)
            .providerKey(fileLocationProvider.getProviderKey())
            .build();
    }

    @Override
    public RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalData) {
        final List<DatasetRetrievalProperties> datasetRetrievalPropertiesList = fileRetrievalData.stream()
                                                                                                 .map(this::buildDatasetRetrievalProperties)
                                                                                                 .collect(Collectors.toList());

        return RetrievalInstructionsResponse.builder()
                                            .datasets(datasetRetrievalPropertiesList)
                                            .providerKey(fileLocationProvider.getProviderKey())
                                            .build();
    }

    private static FileCollectionDmsStorageLocation getFileCollectionDmsStorageLocation(ProviderLocation fileLocation, S3Location unsignedLocation, DpsHeaders headers) {
        FileCollectionDmsStorageLocation dmsLocation = FileCollectionDmsStorageLocation
            .builder()
            .unsignedUrl(fileLocation.getUnsignedUrl())
            .createdAt(fileLocation.getCreatedAt())
            .connectionString(fileLocation.getConnectionString())
            .credentials(fileLocation.getCredentials())
            .createdBy(headers.getUserEmail())
            .region(S3Helper.getBucketRegion(unsignedLocation.getBucket(), fileLocation.getCredentials()))
            .build();
        return dmsLocation;
    }

    private DatasetRetrievalProperties buildDatasetRetrievalProperties(FileRetrievalData fileRetrievalData) {
        final S3Location unsignedLocation = S3Location.of(fileRetrievalData.getUnsignedUrl());
        if (!unsignedLocation.isValid()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(),
                                   "Malformed URL",
                                   "Unsigned URL is invalid, needs to be full S3 storage path");
        }

        final SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
        final RelativeTimeValue relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit(signedUrlParameters.getExpiryTime());
        final long expireInMillis = relativeTimeValue.getTimeUnit().toMillis(relativeTimeValue.getValue());
        final Duration expiration = Duration.ofMillis(expireInMillis);
        final ProviderLocation fileLocation = fileLocationProvider.getFileCollectionRetrievalLocation(unsignedLocation, expiration);
        final FileCollectionDmsStorageLocation dmsLocation = getFileCollectionDmsStorageLocation(fileLocation, unsignedLocation, this.headers);
        final Map<String, Object> downloadLocation = objectMapper.convertValue(dmsLocation, new TypeReference<Map<String, Object>>() {});

        return DatasetRetrievalProperties.builder()
                                         .retrievalProperties(downloadLocation)
                                         .datasetRegistryId(fileRetrievalData.getRecordId())
                                         .build();
    }
}
