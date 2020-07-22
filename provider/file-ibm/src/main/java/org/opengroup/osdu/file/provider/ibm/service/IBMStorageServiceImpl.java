/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.ibm.service;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.opengroup.osdu.file.provider.ibm.security.WhoamiController;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;
import com.ibm.cloud.objectstorage.HttpMethod;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class IBMStorageServiceImpl implements IStorageService {

	private static final Logger log = LoggerFactory.getLogger(IBMStorageServiceImpl.class);
	private final static String URI_EXCEPTION_REASON = "Exception creating signed url";
	private final static String AWS_SDK_EXCEPTION_MSG = "There was an error communicating with the Amazon S3 SDK request "
			+ "for S3 URL signing.";

	// TODO soon tenant factory based on partionid

	@Value("${ibm.cos.signed-url.expiration-days:7}")
	private int s3SignedUrlExpirationTimeInDays;

	@Value("${ibm.cos.region:us-east-1}")
	private String s3Region;

	@Value("${ibm.cos.endpoint_url}")
	private String s3Endpoint;

	@Value("${ibm.cos.access_key}")
	private String accesskey;

	@Value("${ibm.cos.secret_key}")
	private String secret;

	@Value("${ibm.env.prefix:local-dev}")
	private String bucketNamePrefix;

	private ExpirationDateHelper expirationDateHelper;

	@Inject
	private CloudObjectStorageFactory cosFactory;

	private AmazonS3 s3Client;

	@Autowired
	private DpsHeaders headers;

	@Value("${ibm.schemaName}")
	public String DB_NAME;

	@PostConstruct
	public void init() {
		s3Client = cosFactory.getClient();
		expirationDateHelper = new ExpirationDateHelper();
	}

	@Override
	public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
		log.info("Creating the signed blob for fileID : {}. Authorization : {}, partitionID : {}", fileID,
				authorizationToken, partitionID);
		
		SignedUrl url = new SignedUrl();

		try {
			URL s3SignedUrl = generateSignedS3Url(getBucketName(), fileID);
			url.setUri(new URI(s3SignedUrl.toString()));
			url.setUrl(s3SignedUrl);
			url.setCreatedAt(Instant.now());
			url.setCreatedBy(getUserFromToken());
		} catch (Exception e) { // TODO verify exception
			throw new OsduException(URI_EXCEPTION_REASON, e);
		}
		return url;
	}

	public URL generateSignedS3Url(String s3BucketName, String s3ObjectKey) {
		// Set the presigned URL to expire after the amount of time specified by the
		// configuration variables
		Date expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);

		log.info("Requesting a signed S3 URL with an expiration of: " + expiration.toString() + " ("
				+ s3SignedUrlExpirationTimeInDays + " days from now)");

		try {
			Map<String, String> reqParams = new HashMap<String, String>();
			reqParams.put("response-content-type", "application/json");
			//return s3Client.generatePresignedUrl(s3BucketName, s3ObjectKey, expiration);
			return s3Client.generatePresignedUrl(s3BucketName, s3ObjectKey, expiration, HttpMethod.PUT);
		} catch (Exception e) {
			// Catch any SDK client exceptions, and return a 500 error
			log.error("There was an AWS SDK error processing the signing request.");
			throw new OsduException(AWS_SDK_EXCEPTION_MSG, e);
		}
	}

	/*private String getFileLocationPrefix(Instant instant, String filename, String userDesID) {
		String folderName = instant.toEpochMilli() + "-" + DATE_TIME_FORMATTER.withZone(ZoneOffset.UTC).format(instant);

		return format("%s/%s/%s", userDesID, folderName, filename);
	}*/

	public String getBucketName() {
		String partitionId = headers.getPartitionIdWithFallbackToAccountId();
		if (Strings.isNullOrEmpty(partitionId)) {
			throw new AppException(HttpStatus.SC_BAD_REQUEST, "Missing partition id", "");
		}
		return cosFactory.getBucketName(partitionId, DB_NAME);
	}

	public String getUserFromToken() {
		String user = null;
		try {
			user = headers.getUserEmail().split("@")[0];

		} catch (Exception e) {
			throw new OsduException(AWS_SDK_EXCEPTION_MSG, e);
		}
		return user.trim();
	}

}
