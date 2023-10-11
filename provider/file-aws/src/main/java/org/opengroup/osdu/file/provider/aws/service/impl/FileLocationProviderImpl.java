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
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.opengroup.osdu.file.provider.aws.model.S3Location.S3LocationBuilder;
import org.opengroup.osdu.file.provider.aws.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.aws.service.FileLocationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequestScope
public class FileLocationProviderImpl implements FileLocationProvider {

    private static final int RANDOM_KEY_LENGTH = 32;
    private static final String PROVIDER_KEY = "AWS_S3";

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

    @Override
    public ProviderLocation getUploadFileLocation(String fileID, String partitionID) {
        return getUploadLocationInternal(false, fileID, partitionID);
    }

    @Override
    public ProviderLocation getRetrievalFileLocation(S3Location unsignedLocation, Duration expirationDuration) {
        return getRetrievalLocationInternal(false, unsignedLocation, expirationDuration);
    }
    
    @Override
    public ProviderLocation getRetrievalFileLocation(S3Location unsignedLocation, Duration expirationDuration, ResponseHeaderOverrides responseHeaderOverrides) {
        return getRetrievalLocationInternal(false, unsignedLocation, expirationDuration, responseHeaderOverrides);
    }

    @Override
    public ProviderLocation getFileCollectionUploadLocation(String datasetID, String partitionID) {
        return getUploadLocationInternal(true, datasetID, partitionID);
    }

    @Override
    public ProviderLocation getFileCollectionRetrievalLocation(S3Location unsignedLocation, Duration expirationDuration) {
        return getRetrievalLocationInternal(true, unsignedLocation, expirationDuration);
    }

    @Override
    public String getProviderKey() {
        return PROVIDER_KEY;
    }

    private ProviderLocation getUploadLocationInternal(boolean isCollection, String resourceName, String partitionID) {
        final S3ClientConnectionInfo s3ConnectionInfo = s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(headers,
                                                                                                               providerConfigurationBag.bucketParameterRelativePath);
        if (s3ConnectionInfo == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get connection info for S3 bucket");
        }

        final String stsRoleArn = stsRoleHelper.getRoleArnForPartition(headers, providerConfigurationBag.stsRoleIamParameterRelativePath);
        if (stsRoleArn == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get RoleArn to assume using STS for bucket access");
        }

        final S3LocationBuilder s3LocationBuilder = S3Location.newBuilder();
        s3LocationBuilder.withBucket(s3ConnectionInfo.getBucketName())
                         .withFolder(partitionID)
                         .withFolder(RandomStringUtils.randomAlphanumeric(RANDOM_KEY_LENGTH));

        final Duration expirationDuration = Duration.ofDays(providerConfigurationBag.s3SignedUrlExpirationTimeInDays);
        final Date expiration = ExpirationDateHelper.getExpiration(Instant.now(), expirationDuration);
        final S3Location unsignedLocation = s3LocationBuilder.build();

        final String objectKey = unsignedLocation.getKey();
        if (objectKey.length() > StorageConstant.AWS_MAX_KEY_LENGTH) {
            String errorMessage = String.format("The maximum object key length is %s characters, but got %s",
                                                StorageConstant.AWS_MAX_KEY_LENGTH, objectKey.length());
            throw new IllegalArgumentException(errorMessage);
        }

        final TemporaryCredentials credentials = stsCredentialsHelper.getUploadCredentials(unsignedLocation, stsRoleArn, expiration);

        if (isCollection) {
            s3LocationBuilder.withFolder(resourceName);
        } else {
            s3LocationBuilder.withFile(resourceName);
        }

        try {
            final S3Location s3LocationForSignedUpload = s3LocationBuilder.build();
            final URL s3SignedUrl = S3Helper.generatePresignedUrl(s3LocationForSignedUpload, HttpMethod.PUT, expiration, credentials);

            return ProviderLocation.builder()
                                   .unsignedUrl(unsignedLocation.toString())
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

    private void validateInput(boolean isCollection, S3Location unsignedLocation, TemporaryCredentials credentials) {

        validateURL(unsignedLocation);

        validateObjectKey(isCollection, unsignedLocation);
        
        if (isCollection) {
            validateFileCollectionPath(unsignedLocation, credentials);
        } else {
            validateFilePath(unsignedLocation, credentials);
        }
    }
    
    private TemporaryCredentials getTemporaryCredentials(S3Location unsignedLocation, Date expiration) {
        final String stsRoleArn = stsRoleHelper.getRoleArnForPartition(headers, providerConfigurationBag.stsRoleIamParameterRelativePath);
        if (stsRoleArn == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unable to get RoleArn to assume using STS for bucket access");
        }

        
        return stsCredentialsHelper.getRetrievalCredentials(unsignedLocation, stsRoleArn, expiration);
    }
    
    private ProviderLocation getProviderLocation(boolean isCollection, S3Location unsignedLocation, TemporaryCredentials credentials, URL s3SignedUrl) {
    	try {
	    	return ProviderLocation.builder()
	                .unsignedUrl(unsignedLocation.toString())
	                .signedUrl(isCollection ? null : new URI(s3SignedUrl.toString()))
	                .locationSource(unsignedLocation.toString())
	                .credentials(credentials)
	                .connectionString(credentials.toConnectionString())
	                .createdAt(Instant.now())
	                .build();
        } catch (URISyntaxException e) {
            log.error("There was an error generating the URI.", e);
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed S3 URL", "Exception creating signed url", e);
        }
    }
    
    private ProviderLocation getRetrievalLocationInternal(boolean isCollection, S3Location unsignedLocation, Duration expirationDuration, ResponseHeaderOverrides responseHeaderOverrides) {	
    	final Date expiration = ExpirationDateHelper.getExpiration(Instant.now(), expirationDuration);
    	final TemporaryCredentials credentials = getTemporaryCredentials(unsignedLocation, expiration);
    	validateInput(isCollection, unsignedLocation, credentials);

        // Signed URLs only support single files.
        final URL s3SignedUrl = isCollection ? null : S3Helper.generatePresignedUrl(unsignedLocation, HttpMethod.GET, expiration, credentials, responseHeaderOverrides);	
        return getProviderLocation(isCollection, unsignedLocation, credentials, s3SignedUrl);
    }
    
    private ProviderLocation getRetrievalLocationInternal(boolean isCollection, S3Location unsignedLocation, Duration expirationDuration) {
        final Date expiration = ExpirationDateHelper.getExpiration(Instant.now(), expirationDuration);
        final TemporaryCredentials credentials = getTemporaryCredentials(unsignedLocation, expiration);
    	validateInput(isCollection, unsignedLocation, credentials);

        // Signed URLs only support single files.
        final URL s3SignedUrl = isCollection ? null : S3Helper.generatePresignedUrl(unsignedLocation, HttpMethod.GET, expiration, credentials);
        return getProviderLocation(isCollection, unsignedLocation, credentials, s3SignedUrl);
    }

    private void validateURL(S3Location unsignedLocation) {
        if (!unsignedLocation.isValid()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(),
                "Malformed URL",
                "Unsigned URL invalid, needs to be full S3 path");
        }
    }

    private void validateObjectKey(boolean isCollection, S3Location unsignedLocation) {
        if (isCollection) {
            validateFileCollectionObjectKey(unsignedLocation);
        } else {
            validateFileObjectKey(unsignedLocation);
        }
    }

    private void validateFileCollectionPath(S3Location unsignedLocation, TemporaryCredentials credentials) {
        if (!S3Helper.doesObjectCollectionExist(unsignedLocation, credentials)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Invalid/Empty File Collection Path",
                "Invalid/Empty File Collection Path - File collection not found at specified S3 path or is empty");
        }
    }

    private void validateFilePath(S3Location unsignedLocation, TemporaryCredentials credentials) {
        if (!S3Helper.doesObjectExist(unsignedLocation, credentials)) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Invalid File Path",
                "Invalid File Path - File not found at specified S3 path");
        }
    }

    private void validateFileObjectKey(S3Location unsignedLocation) {
        if (unsignedLocation.isFolder()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Invalid S3 Object Key",
                String.format("Invalid S3 Object Key - %s", "Object Key cannot contain trailing '/'"));
        }
    }

    private void validateFileCollectionObjectKey(S3Location unsignedLocation) {
        if (unsignedLocation.isFile()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Invalid S3 Object Key",
                String.format("Invalid S3 Object Key - %s", "Object Key should contain trailing '/'"));
        }
    }
}
