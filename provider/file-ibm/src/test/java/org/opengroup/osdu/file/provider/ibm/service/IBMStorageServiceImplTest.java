package org.opengroup.osdu.file.provider.ibm.service;

import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.file.ReplaceCamelCase;
//import org.mockito.internal.util.reflection.Whitebox;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
//import org.opengroup.osdu.core.aws.entitlements.Authorizer;
//import org.opengroup.osdu.core.common.model.storage.PubSubInfo;
//import org.opengroup.osdu.file.aws.repository.FileLocationDoc;
//import org.opengroup.osdu.file.aws.service.ExpirationDateHelper;
//import org.opengroup.osdu.file.aws.service.StorageServiceImpl;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.ibm.repository.IBMFileRepositoryImpl;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ibm.cloud.objectstorage.HttpMethod;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class IBMStorageServiceImplTest {

	@Mock 
	private SignedUrl signedUrl;
	
	@Mock
	  private DpsHeaders header;

	
	@Mock
	private CloudObjectStorageFactory cosFactory;
	
	@Mock
	private  Logger log ;
  

  @Mock
  private AmazonS3 s3Client;
  

  @Mock
  private IBMFileRepositoryImpl queryHelper;

  private static final String dataPartitionId = "testPartitionId";
  private static final  String BUCKET_NAME_PREFIX = "rs-local-dev";
  private static final String BUCKET = "file-locations";
  @Mock
  private  ExpirationDateHelper expirationDateHelper;
  
  @InjectMocks
  private IBMStorageServiceImpl repo;

  @Before
  public void setUp() {
    initMocks(this);
  }

  // TODO fix this and uncomment
  
  @Test
  public void testCreateSignedUrl() throws IOException {
    // Arrange
	  
	int s3SignedUrlExpirationTimeInDays=7;  
    String testFileId = "testId";
    //https://minio-minio-operator-ns.osdu-dev-869225-a1c3eaf78a86806e299f5f3f207556f0-0000.us-south.containers.appdomain.cloud/test-file-osdu-ibm-storage-file-locations/Rs12455?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20200619T180217Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604799&X-Amz-Credential=minio%2F20200619%2Fus-south%2Fs3%2Faws4_request&X-Amz-Signature=a0d3638425554469bd0a44d705cfb3866a9387a702a4696bf5ca652414874862
    String testAuthToken = "testAuthToken";
       String testUrl = "http://localhost";
    String testUser = "testuser";
    
    String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, dataPartitionId, BUCKET);
    
    String userEmaildID="testuser@abc.com";
   
    Whitebox.setInternalState(repo, "s3SignedUrlExpirationTimeInDays", s3SignedUrlExpirationTimeInDays);
  


    java.util.Date expiration = new java.util.Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * s3SignedUrlExpirationTimeInDays;
    expiration.setTime(expTimeMillis);


    URL url = new URL(testUrl);

    Mockito.when(header.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    Mockito.when(expirationDateHelper.getExpirationDate(s3SignedUrlExpirationTimeInDays)).thenReturn(expiration);
    Mockito.when(header.getUserEmail()).thenReturn(userEmaildID);

	Mockito.when(repo.getBucketName()).thenReturn(bucketName);
	Mockito.when(repo.getUserFromToken()).thenReturn(testUser);
	Mockito.when( s3Client.generatePresignedUrl(bucketName, testFileId, expiration, HttpMethod.PUT)).thenReturn(url);
	
	Mockito.when(repo.generateSignedS3Url(bucketName, testFileId)).thenReturn(url);
	
	
	  //act
	signedUrl=repo.createSignedUrl(testFileId, testAuthToken,dataPartitionId );
    
    // Assert

    Assert.assertEquals(testUser, signedUrl.getCreatedBy());
    Assert.assertEquals(testUrl, signedUrl.getUrl().toString());
  }

}
