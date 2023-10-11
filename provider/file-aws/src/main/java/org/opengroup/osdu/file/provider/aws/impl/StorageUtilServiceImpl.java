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

import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.ChecksumAlgorithm;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.*;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.model.constant.StorageConstant;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Primary
public class StorageUtilServiceImpl implements IStorageUtilService {

    private final DpsHeaders headers;
    private final ProviderConfigurationBag providerConfigurationBag;
    private final StsRoleHelper stsRoleHelper;
    private final StsCredentialsHelper stsCredentialsHelper;
    private final ExpiryTimeUtil expiryTimeUtil;

    @Autowired
    public StorageUtilServiceImpl(ProviderConfigurationBag providerConfigurationBag,
                                    StsCredentialsHelper stsCredentialsHelper,
                                    StsRoleHelper stsRoleHelper,
                                    DpsHeaders headers,
                                    ExpiryTimeUtil expTimeUtil) {
        this.providerConfigurationBag = providerConfigurationBag;
        this.stsCredentialsHelper = stsCredentialsHelper;
        this.stsRoleHelper = stsRoleHelper;
        this.headers = headers;
        this.expiryTimeUtil = expTimeUtil;
    }

    @Override
    public String getStagingLocation(String relativePath, String partitionID) {
        //right now, all AWS impl expect a full S3 path
        return relativePath;
    }

    @Override
    public String getPersistentLocation(String relativePath, String partitionID) {
        // right now, all AWS impl expect a full S3 path. We are not moving files to a 'persistent' location, return original location.
        return relativePath;
    }

    @Override
    public String getChecksum(String filePath) {
        S3Object s3Obj = getS3Object(filePath);

        final long maxBytes = 5368709120L; // 5G
        if (s3Obj.getObjectMetadata().getContentLength() < maxBytes) {
            return calculateChecksum(s3Obj);
        } else {
            return "";
        }
    }

    @Override
    public ChecksumAlgorithm getChecksumAlgorithm() { return ChecksumAlgorithm.MD5; }

    private S3Object getS3Object(String fileLocation) {
        S3Location unsignedLocation = S3Location.of(fileLocation);
        if (!unsignedLocation.isValid()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(),
                "Malformed URL",
                "Unsigned URL invalid, needs to be full S3 path");
        }

        final String stsRoleArn = stsRoleHelper.getRoleArnForPartition(this.headers, providerConfigurationBag.stsRoleIamParameterRelativePath);
        if (stsRoleArn == null) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unable to get RoleArn to assume using STS for bucket access");
        }

        final TemporaryCredentials credentials = stsCredentialsHelper.getRetrievalCredentials(
            unsignedLocation,
            stsRoleArn,
            getTemporaryCredentialsExpirationDate());

        if (!S3Helper.doesObjectExist(unsignedLocation, credentials)) {
            throw new AppException(HttpStatus.NOT_FOUND.value(),
                "S3 object not found",
                "S3 object not found");
        }

        return S3Helper.getObject(unsignedLocation, credentials);
    }

    private Date getTemporaryCredentialsExpirationDate() {
        final ExpiryTimeUtil.RelativeTimeValue relativeTimeValue = expiryTimeUtil.getExpiryTimeValueInTimeUnit(null);
        final long expireInMillis = relativeTimeValue.getTimeUnit().toMillis(relativeTimeValue.getValue());
        final Duration expirationDuration = Duration.ofMillis(expireInMillis);
        return ExpirationDateHelper.getExpiration(Instant.now(), expirationDuration);
    }

    private String calculateChecksum(S3Object s3Obj) {
        try {
            MessageDigest md = MessageDigest.getInstance(getChecksumAlgorithm().toString());
            byte[] bytes = new byte[StorageConstant.AWS_HASH_BYTE_ARRAY_LENGTH];
            int numBytes;
            while ((numBytes = s3Obj.getObjectContent().read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | IOException ex) {
            String message = FileMetadataConstant.CHECKSUM_EXCEPTION + s3Obj.toString();
            throw new AppException(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message , ex.getMessage(), ex);
        }
    }
}
