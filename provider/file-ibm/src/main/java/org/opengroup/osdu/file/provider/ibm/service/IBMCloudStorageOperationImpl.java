/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filecollection.DatasetCopyOperation;
import org.opengroup.osdu.file.provider.interfaces.ICloudStorageOperation;
import org.springframework.stereotype.Service;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;

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
	public List<DatasetCopyOperation> copyDirectories(List<FileCopyOperation> fileCopyOperationList) {
		List<DatasetCopyOperation> datasetCopyOperations = new ArrayList<>();
		for(FileCopyOperation fileCopyOperation : fileCopyOperationList) {
			String[] sourceValues = getFileName(fileCopyOperation.getSourcePath());
			String sourceBucketName = sourceValues[0];
			String sourcKey = sourceValues[1];
		
			String[] destValues = getFileName(fileCopyOperation.getDestinationPath());
			String destinationBucketName = destValues[0];
			String destinationKey = destValues[1];
			List<S3ObjectSummary> s3Objects = s3Client.listObjects(sourceBucketName, sourcKey).getObjectSummaries();
			List<String> keys = s3Objects.stream().map(i -> i.getKey()).collect(Collectors.toList());
			DatasetCopyOperation response;
			try {
//				keys.forEach(key -> s3Client.copyObject(sourceBucketName, key, destinationBucketName, key) );
				for(String key : keys) {
					s3Client.copyObject(sourceBucketName, key, destinationBucketName, key);
				}
				response = DatasetCopyOperation.builder()
	            .fileCopyOperation(fileCopyOperation)
	            .success(true).build();
			} catch (Exception e) {
				response = DatasetCopyOperation.builder()
			            .fileCopyOperation(fileCopyOperation)
			            .success(false).build();
			}
			datasetCopyOperations.add(response);
		}
				
		return datasetCopyOperations;
	}


	@Override
	public Boolean deleteFile(String location) {
		
		String[] deleteValues = getFileName(location);
		String bucketName = deleteValues[0];
		String key = deleteValues[1];
		
		s3Client.deleteObject(bucketName, key);
		return true;
	}
	
	
	  @Override
	  public List<FileCopyOperationResponse> copyFiles(List<FileCopyOperation> fileCopyOperationList) {
	    List<FileCopyOperationResponse> operationResponses = new ArrayList<>();

	    for (FileCopyOperation fileCopyOperation: fileCopyOperationList) {
	      FileCopyOperationResponse response;
	      try {
	        String copyId = this.copyFile(fileCopyOperation.getSourcePath(),
	            fileCopyOperation.getDestinationPath());
	        response = FileCopyOperationResponse.builder()
	            .copyOperation(fileCopyOperation)
	            .success(true).build();
	      } catch (Exception e) {
	        response = FileCopyOperationResponse.builder()
	            .copyOperation(fileCopyOperation)
	            .success(false).build();
	      }
	      operationResponses.add(response);
	    }
	    return operationResponses;
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
