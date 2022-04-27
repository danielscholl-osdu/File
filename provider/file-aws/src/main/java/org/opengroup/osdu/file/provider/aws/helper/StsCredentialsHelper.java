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

        long duration = ((expiration.getTime() - now.toEpochMilli()) / 1_000);
        duration = duration > MAX_DURATION_IN_SECONDS ? MAX_DURATION_IN_SECONDS : duration;

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
        //Some string formats below assume no trailing slash.
        String fileLocationKey = fileLocation.getKey();
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");

        Policy policy = new Policy();

        //Statement 1: Allow Listing files at the file location
        Statement listBucketStatement = new Statement(Statement.Effect.Allow);
        String resource = String.format("arn:aws:s3:::%s", fileLocation.getBucket());
        Condition condition = new Condition()
                                  .withType("StringEquals")
                                  .withConditionKey("s3:prefix")
                                  .withValues(fileLocationKey);

        listBucketStatement = listBucketStatement.withResources(new Resource(resource))
                                                 .withConditions(condition)
                                                 .withActions(S3Actions.ListObjects, S3Actions.ListObjectVersions);

        //Statement 2: Allow Listing files under the file location
        Statement listBucketSubPathStatement = new Statement(Statement.Effect.Allow);
        String resource2 = String.format("arn:aws:s3:::%s", fileLocation.getBucket());
        Condition condition2 = new Condition()
                                   .withType("StringLike")
                                   .withConditionKey("s3:prefix")
                                   .withValues(String.format("%s/*", fileLocationKeyWithoutTrailingSlash));

        listBucketSubPathStatement = listBucketSubPathStatement.withResources(new Resource(resource2))
                                                               .withConditions(condition2)
                                                               .withActions(S3Actions.AllS3Actions);

        //Statement 3: Allow Uploading files at the file location
        Statement allowUploadStatement = new Statement(Statement.Effect.Allow);
        String resource3 = String.format("arn:aws:s3:::%s/%s", fileLocation.getBucket(), fileLocationKey);

        allowUploadStatement = allowUploadStatement.withResources(new Resource(resource3))
                                                   .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads,
                                                                S3Actions.ListMultipartUploadParts, S3Actions.AbortMultipartUpload);

        //Statement 4: Allow Uploading files under the file location
        Statement allowUploadSubPathStatement = new Statement(Statement.Effect.Allow);
        String resource4 = String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash);

        allowUploadSubPathStatement = allowUploadSubPathStatement.withResources(new Resource(resource4))
                                                                 .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads,
                                                                              S3Actions.ListMultipartUploadParts,
                                                                              S3Actions.AbortMultipartUpload);

        return policy.withStatements(listBucketStatement, listBucketSubPathStatement, allowUploadStatement, allowUploadSubPathStatement);
    }

    private Policy createRetrievalPolicy(S3Location fileLocation) {
        String fileLocationKey = fileLocation.getKey();
        String fileLocationKeyWithoutTrailingSlash = fileLocation.getKey().replaceFirst("/$", "");

        Policy policy = new Policy();

        //Statement 1: Allow Listing files at the file location
        Statement listBucketStatement = new Statement(Statement.Effect.Allow);
        String resource = String.format("arn:aws:s3:::%s", fileLocation.getBucket());
        Condition condition = new Condition()
                                  .withType("StringEquals")
                                  .withConditionKey("s3:prefix")
                                  .withValues(fileLocationKey);

        listBucketStatement = listBucketStatement.withResources(new Resource(resource))
                                                 .withConditions(condition)
                                                 .withActions(S3Actions.ListObjects, S3Actions.ListObjectVersions);

        //Statement 2: Allow Listing files under the file location
        Statement listBucketSubPathStatement = new Statement(Statement.Effect.Allow);
        String resource2 = String.format("arn:aws:s3:::%s", fileLocation.getBucket());
        Condition condition2 = new Condition()
                                   .withType("StringLike")
                                   .withConditionKey("s3:prefix")
                                   .withValues(String.format("%s/*", fileLocationKeyWithoutTrailingSlash));

        listBucketSubPathStatement = listBucketSubPathStatement.withResources(new Resource(resource2))
                                                               .withConditions(condition2)
                                                               .withActions(S3Actions.AllS3Actions);

        //Statement 3: Allow Downloading files at the file location
        Statement allowDownloadStatement = new Statement(Statement.Effect.Allow);
        String resource3 = String.format("arn:aws:s3:::%s/%s", fileLocation.getBucket(), fileLocationKey);

        allowDownloadStatement = allowDownloadStatement.withResources(new Resource(resource3))
                                                       .withActions(S3Actions.GetObject, S3Actions.GetObjectVersion);

        //Statement 4: Allow Downloading files under the file location
        Statement allowDownloadSubPathStatement = new Statement(Statement.Effect.Allow);
        String resource4 = String.format("arn:aws:s3:::%s/%s/*", fileLocation.getBucket(), fileLocationKeyWithoutTrailingSlash);

        allowDownloadSubPathStatement = allowDownloadSubPathStatement.withResources(new Resource(resource4))
                                                                     .withActions(S3Actions.GetObject, S3Actions.GetObjectVersion);

        return policy.withStatements(listBucketStatement, listBucketSubPathStatement, allowDownloadStatement,
                                     allowDownloadSubPathStatement);
    }
}
