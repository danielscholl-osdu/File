// Copyright © 2020 Amazon Web Services
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
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.UrlBase64;
import org.joda.time.LocalDate;
import org.opengroup.osdu.core.aws.entitlements.Authorizer;
import org.opengroup.osdu.core.aws.lambda.HttpMethods;
import org.opengroup.osdu.core.aws.s3.S3Config;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.aws.config.AwsServiceConfig;
import org.opengroup.osdu.file.provider.aws.util.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.util.InstantHelper;
import org.opengroup.osdu.file.provider.aws.util.S3Helper;
import org.opengroup.osdu.file.provider.aws.util.STSHelper;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements IStorageService {

//     @Value("${aws.s3.signed-url.expiration-days}")
//   private int s3SignedUrlExpirationTimeInDays;

//   @Value("${aws.s3.datafiles.bucket-name}")
//   private String datafilesBucketName;

//   @Value("${aws.s3.datafiles.path-prefix}")
//   private String s3DatafilePrefix;

//   @Value("${aws.s3.region}")
//   private String s3Region;

//   @Value("${aws.s3.endpoint}")
//   private String s3Endpoint;

//   private AmazonS3 s3Client;

    @Inject
    private DpsHeaders headers;

    @Inject
    private AwsServiceConfig awsServiceConfig;

    @Inject
    private S3Helper s3Helper;

    @Inject
    private STSHelper stsHelper;

    @Inject
    private ExpirationDateHelper expirationDateHelper;


    @Inject 
    private InstantHelper instantHelper;


    private Authorizer authorizer;

    private String roleArn;
    private Duration expirationDuration;
    private String s3DatafilePrefix;

    private final static String AWS_SDK_EXCEPTION_REASON = "AWS SDK Client Exception";
    private final static String AWS_SDK_EXCEPTION_MSG = "There was an error communicating with the Amazon S3 SDK request " +
                "for S3 URL signing.";
    private final static String URI_EXCEPTION_REASON = "Exception creating signed url";
    private final static String INVALID_S3_PATH_REASON = "Unsigned url invalid, needs to be full S3 path";

    @PostConstruct
    public void init() {

        roleArn = awsServiceConfig.stsRoleArn;
        expirationDuration = Duration.ofDays(awsServiceConfig.s3SignedUrlExpirationTimeInDays);
        s3DatafilePrefix = awsServiceConfig.s3DataFilePathPrefix;

        // S3Config config = new S3Config(s3Endpoint, s3Region);
        // s3Client = config.amazonS3();

        authorizer = new Authorizer();

        expirationDateHelper = new ExpirationDateHelper();
    }

        @Override
        public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
            
            if (true)
                throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");
            
            SignedUrl url = new SignedUrl();
            try {

                Instant now = instantHelper.now();
                Date expiration = expirationDateHelper.getExpiration(now, expirationDuration);

                String s3Key = s3DatafilePrefix + partitionID + "/" + fileID;
                String s3Bucket = awsServiceConfig.s3DataFileStagingBucket;

                String unsignedUrl = String.format("s3://%s/%s", s3Bucket, s3Key);

                S3Location fileLocation = new S3Location(unsignedUrl);

                URL s3SignedUrl = s3Helper.generatePresignedUrl(fileLocation, HttpMethod.GET, expiration);
                url.setUri(new URI(s3SignedUrl.toString()));
                url.setUrl(s3SignedUrl);
                url.setCreatedAt(now);
                url.setCreatedBy(getUserFromToken(authorizationToken));

                // String s3Key = s3DatafilePrefix + partitionID + "/" + fileID;
                // URL s3SignedUrl = generateSignedS3Url(datafilesBucketName, s3Key, "https");
                // url.setUri(new URI(s3SignedUrl.toString()));
                // url.setUrl(s3SignedUrl);
                // url.setCreatedAt(Instant.now());
                // url.setCreatedBy(getUserFromToken(authorizationToken));
            } 
            catch(URISyntaxException e){
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), URI_EXCEPTION_REASON);
            }
            return url;
        }

        @Override
        public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken) {
            if (true)
                throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");
            
            return null;
        }  

        /**
     * This method will take a string of a pre-validated S3 bucket name, and use the AWS Java SDK
     * to generate a signed URL with an expiration date set to be as-configured
     * @param s3BucketName - pre-validated S3 bucket name
     * @param s3ObjectKey - pre-validated S3 object key (keys include the path + filename)
     * @return - String of the signed S3 URL to allow file access temporarily
     */
    //   private URL generateSignedS3Url(String s3BucketName, String s3ObjectKey, String httpMethod) {
    //     // Set the presigned URL to expire after the amount of time specified by the configuration variables
    //     Date expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);

    //     log.debug("Requesting a signed S3 URL with an expiration of: " + expiration.toString() + " (" +
    //         s3SignedUrlExpirationTimeInDays + " minutes from now)");

    //     // Generate the presigned URL
    //     GeneratePresignedUrlRequest generatePresignedUrlRequest =
    //         new GeneratePresignedUrlRequest(s3BucketName, s3ObjectKey)
    //             .withMethod(HttpMethod.valueOf(httpMethod))
    //             .withExpiration(expiration);
    //     try {
    //       // Attempt to generate the signed S3 URL
    //       URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    //       return url;
    //     } catch (SdkClientException e) {
    //       // Catch any SDK client exceptions, and return a 500 error
    //       log.error("There was an AWS SDK error processing the signing request.");
    //       throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), AWS_SDK_EXCEPTION_MSG);
    //     }
    //   }

    private String getUserFromToken(String authorizationToken){
        String user;
        try {
        user = authorizer.validateJWT(authorizationToken);
        } catch(IOException e){
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), AWS_SDK_EXCEPTION_MSG);
        }
        return user;
    }
        
    }
