/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

@Service
public class IBMCloudStorageOperationImpl implements ICloudStorageOperation {
	


	@Inject
	private CloudObjectStorageFactory cosFactory;

	private AmazonS3 s3Client;

	
	private final static String INVALID_S3_PATH_REASON = "Unsigned url invalid, needs to be full S3 path";
	
	@PostConstruct
	public void init() {
		s3Client = cosFactory.getClient();
		
	}
	
	
	@Override
	public String copyFile(String sourceFilePath, String destinationFilePath) throws OsduBadRequestException {
		
		
		String[] sourceValues = getFileName(sourceFilePath);
		String sourceBucketName = sourceValues[0];
		String sourcKey = sourceValues[1];
		
		
		String[] destValues = getFileName(destinationFilePath);
		String destinationBucketName = destValues[0];
		String destinationKey = destValues[1];
		
		
		s3Client.copyObject(sourceBucketName, sourcKey, destinationBucketName, destinationKey);
				
		return destinationKey;
	}
	
	@Override
	public Boolean deleteFile(String location) {
		
		String[] deleteValues = getFileName(location);
		String bucketName = deleteValues[0];
		String key = deleteValues[1];
		
		s3Client.deleteObject(bucketName, key);
		return true;
	}
	
	
	
	public String[] getFileName(String unsignedUrl) {
		
		String[] arr = new String[2];
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
		
		arr[0] = bucketName;
		arr[1] = s3Key;
		
        return arr;
		
		
	}
	  

}
