// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.provider.aws.helper;

import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.opengroup.osdu.core.aws.sts.STSConfig;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class StsCredentialsHelper {

    // Role to role chaining limits credential duration to a max of 1 hr
    // https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_terms-and-concepts.html#iam-term-role-chaining
    private static final Integer MAX_DURATION_IN_SECONDS = 3_600;

    private final AWSSecurityTokenService securityTokenService;

    @Autowired
    public StsCredentialsHelper(ProviderConfigurationBag providerConfigurationBag) {
        final STSConfig config = new STSConfig(providerConfigurationBag.amazonRegion);
        this.securityTokenService = config.amazonSTS();
    }

    public TemporaryCredentials getUploadCredentials(S3Location fileLocation, String roleArn, String user, Date expiration) {
        Policy policy = createUploadPolicy(fileLocation);

        return getCredentials(policy, roleArn, user, expiration);
    }

    public TemporaryCredentials getRetrievalCredentials(S3Location fileLocation, String roleArn, String user, Date expiration) {
        Policy policy = createRetrievalPolicy(fileLocation);

        return getCredentials(policy, roleArn, user, expiration);
    }

    public TemporaryCredentials getCredentials(Policy policy, String roleArn, String user, Date expiration) {
        Instant now = Instant.now();
        String roleSessionName = String.format("%s_%s", user, now.toEpochMilli());

        long duration = Math.min(((expiration.getTime() - now.toEpochMilli()) / 1_000), MAX_DURATION_IN_SECONDS);

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
    }

    private Policy createUploadPolicy(S3Location fileLocation) {
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");

        Policy policy = new Policy();

        // Policy Statement needed to create the S3 client.
        Statement getBucketLocationStatement = (new Statement(Statement.Effect.Allow))
            .withResources(new Resource(String.format("arn:aws:s3:::%s", fileLocation.getBucket())))
            .withActions(S3Actions.GetBucketLocation);

        // Policy Statement that lets the user put an object to S3.
        Statement putObjectStatement = (new Statement(Statement.Effect.Allow))
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)))
            .withActions(
                S3Actions.PutObject,
                S3Actions.ListObjects,
                S3Actions.ListObjectVersions,
                S3Actions.ListBucketMultipartUploads,
                S3Actions.ListMultipartUploadParts,
                S3Actions.AbortMultipartUpload);

        return policy.withStatements(getBucketLocationStatement, putObjectStatement);
    }

    private Policy createRetrievalPolicy(S3Location fileLocation) {
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");

        Policy policy = new Policy();

        // Policy Statement needed to perform validation against the s3 bucket.
        Statement bucketValidationStatement = (new Statement(Statement.Effect.Allow))
            .withResources(new Resource(String.format("arn:aws:s3:::%s", fileLocation.getBucket())))
            .withActions(
                S3Actions.GetBucketLocation);

        // Policy Statement that lets the user get objects from S3.
        Statement getObjectStatement = (new Statement(Statement.Effect.Allow))
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)))
            .withActions(
                S3Actions.GetObject,
                S3Actions.GetObjectVersion,
                S3Actions.GetObjectAcl,
                S3Actions.ListObjects,
                S3Actions.ListObjectVersions);

        // Policy Statement that lets the user get object collections from S3.
        Statement getObjectCollectionStatement = (new Statement(Statement.Effect.Allow))
            .withResources(new Resource(String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash)))
            .withActions(
                S3Actions.GetObject,
                S3Actions.GetObjectVersion,
                S3Actions.GetObjectAcl,
                S3Actions.ListObjects,
                S3Actions.ListObjectVersions);

        return policy.withStatements(bucketValidationStatement, getObjectStatement, getObjectCollectionStatement);
    }
}
