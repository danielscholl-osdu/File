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

package org.opengroup.osdu.file.provider.aws.service.impl;

import com.amazonaws.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.s3.util.S3ClientConnectionInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.helper.S3ConnectionInfoHelper;
import org.opengroup.osdu.file.provider.aws.helper.S3Helper;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.helper.StsRoleHelper;
import org.opengroup.osdu.file.provider.aws.model.ProviderLocation;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.aws.service.FileLocationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
@RequestScope
public class FileLocationProviderImpl implements FileLocationProvider {

    private static final String PROVIDER_KEY = "AWS_S3";

    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final DpsHeaders headers;
    private final ProviderConfigurationBag providerConfigurationBag;
    private final StsRoleHelper stsRoleHelper;
    private final StsCredentialsHelper stsCredentialsHelper;
    private final S3ConnectionInfoHelper s3ConnectionInfoHelper;

    @Autowired
    public FileLocationProviderImpl(ProviderConfigurationBag providerConfigurationBag,
                                    StsCredentialsHelper stsCredentialsHelper,
                                    StsRoleHelper stsRoleHelper,
                                    S3ConnectionInfoHelper s3ConnectionInfoHelper,
                                    DpsHeaders headers) {
        this.providerConfigurationBag = providerConfigurationBag;
        this.stsCredentialsHelper = stsCredentialsHelper;
        this.stsRoleHelper = stsRoleHelper;
        this.s3ConnectionInfoHelper = s3ConnectionInfoHelper;
        this.headers = headers;
    }

    private static String generateUniqueKey() {
        byte[] buffer = new byte[20];
        random.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }

    @Override
    public ProviderLocation getFileLocation(String fileID, String partitionID) {
        return getLocationInternal(false, fileID, partitionID);
    }

    @Override
    public ProviderLocation getFileLocation(S3Location unsignedLocation, String fileID, Duration expirationDuration) {
        return getLocationInternal(false, unsignedLocation, fileID, expirationDuration);
    }

    @Override
    public ProviderLocation getFileCollectionLocation(String datasetID, String partitionID) {
        return getLocationInternal(true, datasetID, partitionID);
    }

    @Override
    public ProviderLocation getFileCollectionLocation(S3Location unsignedLocation, String datasetID, Duration expirationDuration) {
        return getLocationInternal(true, unsignedLocation, datasetID, expirationDuration);
    }

    @Override
    public String getProviderKey() {
        return PROVIDER_KEY;
    }

    private ProviderLocation getLocationInternal(boolean isCollection, String resourceName, String partitionID) {
        final S3ClientConnectionInfo s3ConnectionInfo = s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(headers,
                                                                                                               providerConfigurationBag.bucketParameterRelativePath);
        if (s3ConnectionInfo == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get connection info for S3 bucket");
        }

        final String objectKey = String.format("%s/%s", partitionID, generateUniqueKey());
        if (objectKey.length() > StorageConstant.AWS_MAX_KEY_LENGTH) {
            String errorMessage = String.format("The maximum object key length is %s characters, but got %s",
                                                StorageConstant.AWS_MAX_KEY_LENGTH, objectKey.length());
            throw new IllegalArgumentException(errorMessage);
        }

        final String bucketUnsignedUrl = String.format("s3://%s/%s/", s3ConnectionInfo.getBucketName(), objectKey);
        final S3Location unsignedLocation = S3Location.of(bucketUnsignedUrl);
        final Duration expirationDuration = Duration.ofDays(providerConfigurationBag.s3SignedUrlExpirationTimeInDays);

        return getLocationInternal(isCollection, unsignedLocation, resourceName, expirationDuration);
    }


    private ProviderLocation getLocationInternal(boolean isCollection, S3Location unsignedLocation, String resourceName,
                                                 Duration expirationDuration) {
        final String stsRoleArn = stsRoleHelper.getRoleArnForPartition(headers, providerConfigurationBag.stsRoleIamParameterRelativePath);
        if (stsRoleArn == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get RoleArn to assume using STS for bucket access");
        }

        final Date expiration = ExpirationDateHelper.getExpiration(Instant.now(), expirationDuration);
        final TemporaryCredentials credentials = stsCredentialsHelper.getUploadCredentials(unsignedLocation, stsRoleArn,
                                                                                           headers.getUserEmail(),
                                                                                           expiration);

        try {
            // Signed urls only support a single file. If user chooses to use the signedURL approach,
            // file will just be called signedUpload and need to be renamed later if it should be meaningful.
            String unsignedUrl = unsignedLocation.toString();
            if (!unsignedUrl.endsWith("/")) {
                unsignedUrl += "/"; // need to add '/' to the end of the URL for both: files and collections.
            }

            String urlForSignedUpload = unsignedUrl + resourceName;
            if (isCollection) {
                urlForSignedUpload += "/"; // '/' needs to be added at the end of the URL for collection.
            }

            final S3Location s3LocationForSignedUpload = S3Location.of(urlForSignedUpload);
            final URL s3SignedUrl = S3Helper.generatePresignedUrl(s3LocationForSignedUpload, HttpMethod.PUT, expiration, credentials);

            return ProviderLocation.builder()
                                   .unsignedUrl(unsignedUrl)
                                   .signedUrl(new URI(s3SignedUrl.toString()))
                                   .locationSource(s3LocationForSignedUpload.toString())
                                   .credentials(credentials)
                                   .connectionString(credentials.toConnectionString())
                                   .createdAt(Instant.now())
                                   .build();
        } catch (URISyntaxException e) {
            log.error("There was an error generating the URI.", e);
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed S3 URL", "Exception creating signed url", e);
        }
    }
}
