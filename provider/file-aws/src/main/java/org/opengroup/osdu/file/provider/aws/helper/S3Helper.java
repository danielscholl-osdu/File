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

package org.opengroup.osdu.file.provider.aws.helper;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Region;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentialsProvider;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

import java.net.URL;
import java.util.Date;

public class S3Helper {

    /**
     * Generates a presigned URL for the S3 location
     */
    public static URL generatePresignedUrl(S3Location location,
                                           HttpMethod httpMethod,
                                           Date expiration,
                                           TemporaryCredentials credentials) throws SdkClientException {
        final AmazonS3 s3 = generateClientWithCredentials(location.getBucket(), credentials);
        final GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(location.getBucket(),
                                                                                                        location.getKey(),
                                                                                                        httpMethod)
                                                                            .withExpiration(expiration);

        return s3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public static boolean doesObjectExist(S3Location location, TemporaryCredentials credentials) {
        AmazonS3 s3 = generateClientWithCredentials(location.getBucket(), credentials);
        try {
            return s3.doesObjectExist(location.getBucket(), location.getKey());
        } catch (AmazonServiceException exception) {
            return false;
        }
    }

    public static boolean doesObjectCollectionExist(S3Location location, TemporaryCredentials credentials) {
        AmazonS3 s3 = generateClientWithCredentials(location.getBucket(), credentials);
        try {
            final ObjectListing s3ObjectList = s3.listObjects(location.getBucket(), location.getKey());
            return !s3ObjectList.getObjectSummaries().isEmpty();
        } catch (AmazonServiceException exception) {
            return false;
        }
    }

    public static String getBucketRegion(String bucket, TemporaryCredentials credentials) {
        final AmazonS3 simpleS3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new TemporaryCredentialsProvider(credentials))
            .build();
        final String bucketLocation = simpleS3Client.getBucketLocation(bucket);
        final Region s3Region = Region.fromValue(bucketLocation);

        return s3Region.toAWSRegion().getName();
    }

    private static AmazonS3 generateClientWithCredentials(String bucket, TemporaryCredentials credentials) {
        final String region = getBucketRegion(bucket, credentials);

        return AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.fromName(region))
                                    .withCredentials(new TemporaryCredentialsProvider(credentials))
                                    .build();
    }
}
