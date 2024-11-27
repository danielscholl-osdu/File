/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.file.provider.aws.helper;
import java.util.UUID;
import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.opengroup.osdu.core.aws.sts.STSConfig;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class StsCredentialsHelper {

    private final AWSSecurityTokenService securityTokenService;

    @Autowired
    public StsCredentialsHelper(ProviderConfigurationBag providerConfigurationBag) {
        final STSConfig config = new STSConfig(providerConfigurationBag.amazonRegion);
        this.securityTokenService = config.amazonSTS();
    }

    public TemporaryCredentials getUploadCredentials(S3Location fileLocation, String roleArn, Date expiration) {
        Policy policy = createUploadPolicy(fileLocation);

        return getCredentials(policy, roleArn, expiration);
    }

    public TemporaryCredentials getRetrievalCredentials(S3Location fileLocation, String roleArn, Date expiration) {
        Policy policy = createRetrievalPolicy(fileLocation);

        return getCredentials(policy, roleArn, expiration);
    }

    public TemporaryCredentials getCredentials(Policy policy, String roleArn, Date expiration) {
        Instant now = Instant.now();
        UUID uuid = UUID.randomUUID();
        String roleSessionName = uuid.toString();
        long duration = Math.round(((expiration.getTime() - now.toEpochMilli()) / 1_000.0) / 60.0) * 60;

        try {
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withRoleSessionName(roleSessionName)
                .withDurationSeconds((int) duration)
                .withPolicy(policy.toJson());

            AssumeRoleResult response = securityTokenService.assumeRole(roleRequest);
            Credentials sessionCredentials = response.getCredentials();

            return TemporaryCredentials.builder()
                .accessKeyId(sessionCredentials.getAccessKeyId())
                .expiration(sessionCredentials.getExpiration())
                .secretAccessKey(sessionCredentials.getSecretAccessKey())
                .sessionToken(sessionCredentials.getSessionToken())
                .build();

        } catch (AWSSecurityTokenServiceException e) {
            throw new OsduBadRequestException("Failed to assume role: " + e.getMessage(), e);
        }
    }

    private Policy createUploadPolicy(S3Location fileLocation) {
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");
        return (new Policy()).withStatements(
            allowUserToSeeBucketList(),
            allowRootAndLocationListing(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowSubLocationListing(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowSubLocationGet(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowLocationGet(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowLocationPut(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowSubLocationPut(fileLocation, fileLocationKeyWithoutTrailingSlash)
        );
    }

    private Policy createRetrievalPolicy(S3Location fileLocation) {
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");
        return new Policy().withStatements(
            allowUserToSeeBucketList(),
            allowRootAndLocationListing(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowSubLocationListing(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowSubLocationGet(fileLocation, fileLocationKeyWithoutTrailingSlash),
            allowLocationGet(fileLocation, fileLocationKeyWithoutTrailingSlash)
        );
    }

    private Statement allowUserToSeeBucketList() {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.ListBuckets, S3Actions.GetBucketLocation)
            .withResources(new Resource("arn:aws:s3:::*"));
    }

    private Statement allowRootAndLocationListing(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.ListBuckets, S3Actions.ListObjects, S3Actions.ListObjectVersions)
            .withResources(new Resource(String.format("arn:aws:s3:::%s", fileLocation.getBucket())))
            .withConditions(new Condition().withType("StringEquals").withConditionKey("s3:prefix").withValues("", fileLocationKeyWithoutTrailingSlash));
    }

    private Statement allowSubLocationListing(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.ListBuckets, S3Actions.ListObjects, S3Actions.ListObjectVersions)
            .withResources(new Resource(String.format("arn:aws:s3:::%s", fileLocation.getBucket())))
            .withConditions(new Condition().withType("StringLike").withConditionKey("s3:prefix").withValues(String.format("%s/*", fileLocationKeyWithoutTrailingSlash)));
    }

    private Statement allowLocationGet(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.GetObject, S3Actions.GetObjectVersion, S3Actions.GetObjectAcl)
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)));
    }

    private Statement allowSubLocationGet(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.GetObject, S3Actions.GetObjectVersion, S3Actions.GetObjectAcl)
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)));
    }

    private Statement allowLocationPut(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads, S3Actions.ListMultipartUploadParts, S3Actions.AbortMultipartUpload)
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)));
    }

    private Statement allowSubLocationPut(S3Location fileLocation, String fileLocationKeyWithoutTrailingSlash) {
        return (new Statement(Statement.Effect.Allow))
            .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads, S3Actions.ListMultipartUploadParts, S3Actions.AbortMultipartUpload)
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)));
    }
}
