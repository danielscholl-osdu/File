/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.service;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.provider.interfaces.IFileCollectionStorageUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Strings;

/**
 * @author 000LBT744
 *
 */

@Service
@Primary
public class FileCollectionStorageUtilServiceImpl implements IFileCollectionStorageUtilService {
	@Value("${ibm.persistent.bucket}")
	private String persistentBucket;

	@Value("${ibm.staging.bucket}")
	private String stagingBucket;

	@Inject
	private CloudObjectStorageFactory cosFactory;

	@Autowired
	private DpsHeaders headers;

	/*
	 * format of s3 s3://bucketname/Dir-key-name/filename
	 */
	
	
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

}

