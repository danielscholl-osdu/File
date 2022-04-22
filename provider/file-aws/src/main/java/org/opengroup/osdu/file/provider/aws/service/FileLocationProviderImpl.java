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

import com.amazonaws.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.s3.util.S3ClientConnectionInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.AwsServiceConfig;
import org.opengroup.osdu.file.provider.aws.di.IFileLocationProvider;
import org.opengroup.osdu.file.provider.aws.model.FileUploadLocation;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.util.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.util.InstantHelper;
import org.opengroup.osdu.file.provider.aws.util.S3ConnectionInfoHelper;
import org.opengroup.osdu.file.provider.aws.util.S3Helper;
import org.opengroup.osdu.file.provider.aws.util.STSCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.util.STSRoleHelper;
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
public class FileLocationProviderImpl implements IFileLocationProvider {

    private final static String URI_EXCEPTION_REASON = "Exception creating signed url";
    private final static String SIGNED_UPLOAD_FILE_NAME = "signedUpload";

    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private final DpsHeaders headers;
    private final AwsServiceConfig awsServiceConfig;
    private final STSRoleHelper stsRoleHelper;
    private final InstantHelper instantHelper;
    private final ExpirationDateHelper expirationDateHelper;
    private final STSCredentialsHelper stsCredentialsHelper;
    private final S3Helper s3Helper;
    private final S3ConnectionInfoHelper s3ConnectionInfoHelper;

    @Autowired
    public FileLocationProviderImpl(AwsServiceConfig awsServiceConfig,
                                    InstantHelper instantHelper,
                                    ExpirationDateHelper expirationDateHelper,
                                    STSCredentialsHelper stsCredentialsHelper,
                                    STSRoleHelper stsRoleHelper,
                                    S3Helper s3Helper,
                                    S3ConnectionInfoHelper s3ConnectionInfoHelper,
                                    DpsHeaders headers) {
        this.awsServiceConfig = awsServiceConfig;
        this.instantHelper = instantHelper;
        this.expirationDateHelper = expirationDateHelper;
        this.stsCredentialsHelper = stsCredentialsHelper;
        this.stsRoleHelper = stsRoleHelper;
        this.s3Helper = s3Helper;
        this.s3ConnectionInfoHelper = s3ConnectionInfoHelper;
        this.headers = headers;
    }

    private static String generateUniqueKey() {
        byte[] buffer = new byte[20];
        random.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }

    @Override
    public FileUploadLocation getUploadLocation(String fileID, String partitionID) {
        S3ClientConnectionInfo s3ConnectionInfo = s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(headers,
                                                                                                         awsServiceConfig.bucketParameterRelativePath);

        if (s3ConnectionInfo == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get connection info for S3 bucket");
        }

        String stsRoleArn = stsRoleHelper.getRoleArnForPartition(headers, awsServiceConfig.stsRoleIamParameterRelativePath);

        if (stsRoleArn == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                   "Unable to get RoleArn to assume using STS for bucket access");
        }

        // filepath is {data-partition}/{key-path}
        // unsignedUrl is s3://{bucket-name}/{filepath}/
        String unsignedUrl = String.format("s3://%s/%s/%s/", s3ConnectionInfo.getBucketName(), partitionID, generateUniqueKey());
        S3Location s3Location = new S3Location(unsignedUrl);

        Instant now = instantHelper.now();
        Duration expirationDuration = Duration.ofDays(awsServiceConfig.s3SignedUrlExpirationTimeInDays);
        Date expiration = expirationDateHelper.getExpiration(now, expirationDuration);
        TemporaryCredentials credentials = stsCredentialsHelper.getUploadCredentials(s3Location, stsRoleArn,
                                                                                     headers.getUserEmail(),
                                                                                     expiration);

        try {
            // Signed urls only support a single file. If user chooses to use the signedURL approach,
            // file will just be called signedUpload and need to be renamed later if it should be meaningful.
            S3Location s3LocationForSignedUpload = new S3Location(unsignedUrl + fileID);
            URL s3SignedUrl = s3Helper.generatePresignedUrl(s3LocationForSignedUpload, HttpMethod.PUT, expiration, credentials);

            return FileUploadLocation.builder()
                                     .unsignedUrl(unsignedUrl)
                                     .signedUrl(new URI(s3SignedUrl.toString()))
                                     .signedUploadFileName(fileID)
                                     .credentials(credentials)
                                     .connectionString(credentials.toConnectionString())
                                     .createdAt(Instant.now())
                                     .region(s3ConnectionInfo.getRegion())
                                     .build();

        } catch (URISyntaxException e) {
            log.error("There was an error generating the URI.", e);
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Malformed S3 URL", URI_EXCEPTION_REASON, e);
        }
    }

    @Override
    public String getProviderKey() {
        return null;
    }
}
