/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;


import java.util.Arrays;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.constant.ChecksumAlgorithm;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IBMStorageUtilServiceImpl implements IStorageUtilService{
	
	@Value("${ibm.persistent.bucket}")
	private String persistentBucket;
	
	@Value("${ibm.staging.bucket}")
	private String stagingBucket;

	
	
	@Inject
	private CloudObjectStorageFactory cosFactory;
	
	@Autowired
	private DpsHeaders headers;
	
	
	
	// format of s3 s3://bucketname/filename
	
	 @Override
	  public String getPersistentLocation(String relativePath, String partitionId) {
		 
		 String bucketName = getBucketName(persistentBucket);
		 
		 return "s3://"+bucketName+"/"+relativePath;
	  }

	  @Override
	  public String getStagingLocation(String relativePath, String partitionId) {
		  
		  String bucketName = getBucketName(stagingBucket);
	   
		  return "s3://"+bucketName+"/"+relativePath;
	  }
	  
	  
	  public String getBucketName(String bucket) {
			String partitionId = headers.getPartitionIdWithFallbackToAccountId();
			if (Strings.isNullOrEmpty(partitionId)) {
				throw new AppException(HttpStatus.SC_BAD_REQUEST, "Missing partition id", "");
			}
			return cosFactory.getBucketName(partitionId, bucket);
		}

	  
	  @Override
	  public String getChecksum(final String filePath) {
		  if (Strings.isNullOrEmpty(filePath)) {
		      throw new AppException(HttpStatus.SC_BAD_REQUEST, String.format("Illegal file path argument - { %s }", filePath), "");
		    }
		  
		  String[] filePath_split = Arrays.stream(filePath.split("/"))
				  .map(String::trim)
				  .toArray(String[]::new);
		  		  
		  Map<String, Object> rawMetadata = cosFactory.getClient().getObjectMetadata(filePath_split[2], filePath_split[3]).getRawMetadata();		 		  
		  return rawMetadata.get("etag").toString();
	  }
	  
	  @Override
	  public ChecksumAlgorithm getChecksumAlgorithm() { 
		  return ChecksumAlgorithm.MD5; 
	  }
}
