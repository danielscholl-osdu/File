package org.opengroup.osdu.file.provider.ibm.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;

@ExtendWith(MockitoExtension.class)
public class IBMStorageUtilServiceImplTest {
	
	@InjectMocks
    private IBMStorageUtilServiceImpl storageUtilService = new IBMStorageUtilServiceImpl();
	
	@Mock
	CloudObjectStorageFactory cosFactory;
	
	@Mock
	AmazonS3 s3Client;
	
	@Mock
	ObjectMetadata objMetadata;
	
	
	 @Test
	 public void getChecksum_ShouldCall_CloudStoregetObjectMetadataMethod() {
		 String filepath="s3://test-bucket/file_source";
		 Map<String, Object> rawMetadata = new HashMap<>();
		 rawMetadata.put("etag", "f7ef8184072611b0c12470dbb8bb6d37");
		 
		 Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		 Mockito.when(cosFactory.getClient().getObjectMetadata("test-bucket", "file_source")).thenReturn(objMetadata);
		 Mockito.when(cosFactory.getClient().getObjectMetadata("test-bucket", "file_source").getRawMetadata()).thenReturn(rawMetadata);
		 
		 String checksum = storageUtilService.getChecksum(filepath);
		 
		 Assert.assertEquals("f7ef8184072611b0c12470dbb8bb6d37", checksum);
		 
	 }
	
	 @Test
	  public void getChecksum_throwsAppException_ifFilePathIsEmpty() {
			try {
				String filepath = "";
				storageUtilService.getChecksum(filepath);
				fail("Should not succeed, as filepath is empty");

			} catch (AppException e) {
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getError().getCode());
	            assertEquals("Illegal file path argument - {  }", e.getError().getReason());

			}
		 
	 }
	 
	 @Test
	  public void getChecksum_throwsAppException_ifFilePathIsNull() {
		 try {
				String filepath = null;
				storageUtilService.getChecksum(filepath);
				fail("Should not succeed, as filepath is null");

			} catch (AppException e) {
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getError().getCode());
	            assertEquals("Illegal file path argument - { null }", e.getError().getReason());

			}
	 }

}
