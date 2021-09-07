/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import java.net.URI;
import java.net.URL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.opengroup.osdu.file.provider.ibm.security.WhoamiController;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.exception.OsduUnauthorizedException;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.ibm.model.file.S3Location;
import org.opengroup.osdu.file.provider.ibm.model.file.TemporaryCredentials;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;
import com.ibm.cloud.objectstorage.HttpMethod;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;


import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import com.ibm.cloud.objectstorage.services.s3.model.GeneratePresignedUrlRequest;

@Service
@Log
@RequiredArgsConstructor
public class IBMStorageServiceImpl implements IStorageService {

	private static final Logger log = LoggerFactory.getLogger(IBMStorageServiceImpl.class);
	private final static String URI_EXCEPTION_REASON = "Exception creating signed url";
	private final static String AWS_SDK_EXCEPTION_MSG = "There was an error communicating with the Amazon S3 SDK request "
			+ "for S3 URL signing.";
	private final static String INVALID_S3_PATH_REASON = "Unsigned url invalid, needs to be full S3 path";
	private static final String s3UploadLocationUriFormat = "s3://%s/%s";
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
	
	@Value("${PROVIDER_KEY}")
	private String providerKey;
	
	private String roleArn;

	private ExpirationDateHelper expirationDateHelper;
	
	private SignedUrlParameters signedParam;

	@Inject
	private CloudObjectStorageFactory cosFactory;

	private AmazonS3 s3Client;

	@Autowired
	private DpsHeaders headers;

	@Autowired
	private TenantInfo tenant;
	
	@Inject
	private STSHelper stsHelper;


	@Value("${ibm.staging.bucket}")
	public String DB_NAME;

	@PostConstruct
	public void init() {
		s3Client = cosFactory.getClient();
		roleArn = "arn:123:456:789:1234";
		expirationDateHelper = new ExpirationDateHelper();
		signedParam= new SignedUrlParameters();
	}

	@Override
	public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
		log.info("Creating the signed blob for fileID : {}.  partitionID : {}", fileID,	partitionID);
		try {
		tenant.getName();
		} catch (Exception e) {

			 throw new OsduUnauthorizedException("Unauthorized");
		}

		SignedUrl url = new SignedUrl();

		try {
			URL s3SignedUrl = generateSignedS3Url(getBucketName(), fileID);
			url.setUri(new URI(s3SignedUrl.toString()));
			url.setUrl(s3SignedUrl);
			url.setCreatedAt(Instant.now());
			url.setCreatedBy(getUserFromToken());
			url.setFileSource(fileID);
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

	 @Override
   public SignedUrl createSignedUrlFileLocation(String unsignedUrl,
       String authorizationToken, SignedUrlParameters signedUrlParameters) {

		 // "unsignedUrl": "s3://osdu-seismic-test-data/r1/data/provided/trajectories/1537.csv"
		 log.info("Creating the signed url for unsugnedurl : {}. Authorization : {}, partitionID : {}", unsignedUrl);
		 try {
				tenant.getName();
				} catch (Exception e) {

					 throw new OsduUnauthorizedException("Unauthorized");
				}

		 String[] s3PathParts = unsignedUrl.split("s3://");
			if (s3PathParts.length < 2) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed URL", INVALID_S3_PATH_REASON);
			}

			String[] s3ObjectKeyParts = s3PathParts[1].split("/");
			if (s3ObjectKeyParts.length < 1) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed URL", INVALID_S3_PATH_REASON);
			}

			String bucketName = s3ObjectKeyParts[0];
			String s3Key = String.join("/", Arrays.copyOfRange(s3ObjectKeyParts, 1, s3ObjectKeyParts.length));

     URL s3SignedUrl = generateSignedS3DownloadUrl(bucketName, s3Key, "GET", signedUrlParameters);

			 return SignedUrl.builder()
			          .url(s3SignedUrl)
			          .build();



	 }

	 /**
		 * This method will take a string of a pre-validated S3 bucket name, and use the
		 * AWS Java SDK to generate a signed URL with an expiration date based on
		 * signedUrl params provided
		 *
		 * @param s3BucketName - pre-validated S3 bucket name
		 * @param s3ObjectKey  - pre-validated S3 object key (keys include the path +
		 *                     filename)
     * @param signedUrlParameters - params(like expiry time) to be set for signedUrl
		 * @return - String of the signed S3 URL to allow file access temporarily
		 */

   private URL generateSignedS3DownloadUrl(String s3BucketName, String s3ObjectKey,
       String httpMethod, SignedUrlParameters signedUrlParameters) {
		// TODO Auto-generated method stub
     Date expiration;
     if (signedUrlParameters.getExpiryTime() != null) {
       expiration = expirationDateHelper.getExpirationTime(signedUrlParameters.getExpiryTime());
     } else {
       expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);
     }
		log.debug("Requesting a signed S3 URL with an expiration of: " + expiration.toString() + " ("
				+ s3SignedUrlExpirationTimeInDays + " minutes from now)");

		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(s3BucketName, s3ObjectKey)
				.withMethod(HttpMethod.valueOf(httpMethod)).withExpiration(expiration);

		try {
			// Attempt to generate the signed S3 URL
			URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
			return url;
		} catch (SdkClientException e) {
			// Catch any SDK client exceptions, and return a 500 error
			log.error("There was an AWS SDK error processing the signing request.", e);
			throw new AppException(HttpStatus.SC_SERVICE_UNAVAILABLE, "Remote Service Unavailable",
					AWS_SDK_EXCEPTION_MSG, e);
		}


	}
   
   @Override
	public StorageInstructionsResponse createStorageInstructions(String fileID, String partitionID) {
		
		log.info("calling StorageInstructions to create temporaryCredentials and signed url for file upload");
		StorageInstructionsResponse storageInstructionsResponse = new StorageInstructionsResponse();
		Map<String, Object> storageLocation = new HashMap<String, Object>();
		Date expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);
		String unsignedUrl = String.format(s3UploadLocationUriFormat, getBucketName(), fileID);
		S3Location s3Location = new S3Location(unsignedUrl);
		URL signedUrl=generateSignedS3Url(getBucketName(), fileID);
		TemporaryCredentials credentials = stsHelper.getUploadCredentials(s3Location, roleArn,
				this.headers.getUserEmail(), expiration);
		log.debug("temporaryCredentials for file upload:", credentials);
		storageLocation.put("unsignedUrl", unsignedUrl);
		storageLocation.put("signedUrl", signedUrl);
		storageLocation.put("createdAt", Instant.now());
		storageLocation.put("connectionString", credentials.toConnectionString());
		storageLocation.put("credentials", credentials);
		storageLocation.put("region", s3Region);
		storageLocation.put("fileSource", fileID);
		storageInstructionsResponse.setStorageLocation(storageLocation);
		storageInstructionsResponse.setProviderKey(providerKey);
		log.debug("signedUrl for file upload:",signedUrl);
		return storageInstructionsResponse;
	}
	
	@Override
	public RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalData) {
	
		log.info("calling Retrieval Instructions to generate temporaryCredentials and signed url for file download");	
		RetrievalInstructionsResponse response = new RetrievalInstructionsResponse();
		Map<String, Object> retrivalDataSet = new HashMap<String, Object>();
		DatasetRetrievalProperties dataset = new DatasetRetrievalProperties();
		List<DatasetRetrievalProperties> listOfdDataSet = new ArrayList<DatasetRetrievalProperties>();
		Instant currentTime = Instant.now();
		Date expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);
		for (FileRetrievalData retrivaldata : fileRetrievalData) {
			S3Location fileLocation = new S3Location(retrivaldata.getUnsignedUrl());
			if (!fileLocation.isValid()) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed URL", INVALID_S3_PATH_REASON);
			}
			if (fileLocation.getKey().trim().endsWith("/")) {
				throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid S3 Object Key",
						"Invalid S3 Object Key - Object key cannot contain trailing '/'");
			}
			TemporaryCredentials credentials = stsHelper.getRetrievalCredentials(fileLocation, roleArn,
					this.headers.getUserEmail(), expiration);
			SignedUrlParameters signedParam=new SignedUrlParameters();
			SignedUrl signedUrl=createSignedUrlFileLocation(retrivaldata.getUnsignedUrl(), headers.getAuthorization(),signedParam);
			retrivalDataSet.put("unsignedUrl", retrivaldata.getUnsignedUrl());
			retrivalDataSet.put("signedUrl",signedUrl.getUrl().toString());
			retrivalDataSet.put("signedUrlExpiration", expiration);
			retrivalDataSet.put("connectionString", credentials.toConnectionString());
			retrivalDataSet.put("credentials", credentials);
			retrivalDataSet.put("createdAt", currentTime);
			retrivalDataSet.put("createdAt", Instant.now());
			retrivalDataSet.put("region", s3Region);
			dataset.setRetrievalProperties(retrivalDataSet);
			dataset.setDatasetRegistryId(retrivaldata.getRecordId());
			listOfdDataSet.add(dataset);
			response.setDatasets(listOfdDataSet);
			response.setProviderKey(providerKey);
		}
		return response;
	}


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
