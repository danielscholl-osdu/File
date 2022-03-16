/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.provider.ibm.model.file.S3Location;
import org.opengroup.osdu.file.provider.ibm.model.file.TemporaryCredentials;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 000LBT744
 *
 */

@Service
@Primary
@Slf4j
public class FileCollectionStorageServiceImpl implements IFileCollectionStorageService {

	private static final String s3UploadLocationUriFormat = "s3://%s/%s";
    private ExpirationDateHelper expirationDateHelper = new ExpirationDateHelper();
    private final static String INVALID_S3_PATH_REASON = "Unsigned url invalid, needs to be full S3 path";
    
    
    @Value("${ibm.cos.signed-url.expiration-days:7}")
	private int s3SignedUrlExpirationTimeInDays;
    
    @Value("${ibm.staging.bucket}")
	public String DB_NAME;
    
	@Inject
    private DpsHeaders headers;
	
	@Value("${PROVIDER_KEY}")
    private String providerKey;
	
	@Inject
    private STSHelper stsHelper;
	
	private String roleArn = "arn:123:456:789:1234";
	
	@Inject
	private CloudObjectStorageFactory cosFactory;
	
	
	@Override
	public StorageInstructionsResponse createStorageInstructions(String directoryID, String partitionID) {
		// s3://{bucket-name}/{data-partition}/{key-path}/
		//String unsignedUrl = String.format(s3UploadLocationUriFormat, "upload-bucket", headers.getPartitionId(), generateUniqueKey());
		String unsignedUrl = String.format(s3UploadLocationUriFormat, getBucketName(), directoryID);
		S3Location s3Location = new S3Location(unsignedUrl);
		Date expiration = expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays);
		TemporaryCredentials credentials = stsHelper.getUploadCredentials(s3Location, roleArn, this.headers.getUserEmail(), expiration);
		Map<String, Object> storageLocation = new HashMap<String, Object>();
		storageLocation.put("connectionString", credentials.toConnectionString());
		storageLocation.put("credentials", credentials);
		storageLocation.put("unsignedUrl", unsignedUrl);
		return StorageInstructionsResponse.builder().storageLocation(storageLocation).providerKey(providerKey).build();

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
			try {
			TemporaryCredentials credentials = stsHelper.getRetrievalCredentials(fileLocation, roleArn,
					this.headers.getUserEmail(), expiration);
			retrivalDataSet.put("unsignedUrl", retrivaldata.getUnsignedUrl());
			retrivalDataSet.put("connectionString", credentials.toConnectionString());
			retrivalDataSet.put("credentials", credentials);
			retrivalDataSet.put("createdAt", currentTime);
			retrivalDataSet.put("createdAt", Instant.now());
			dataset.setRetrievalProperties(retrivalDataSet);
			dataset.setDatasetRegistryId(retrivaldata.getRecordId());
			listOfdDataSet.add(dataset);
			response.setDatasets(listOfdDataSet);
			response.setProviderKey(providerKey);
			}catch (Exception e) {
				throw new NullPointerException("data cannot be null");
			}
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

	
	

}
