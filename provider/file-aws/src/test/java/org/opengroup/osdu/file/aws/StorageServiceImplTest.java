package org.opengroup.osdu.file.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.entitlements.Authorizer;
import org.opengroup.osdu.core.common.model.storage.PubSubInfo;
import org.opengroup.osdu.file.aws.repository.FileLocationDoc;
import org.opengroup.osdu.file.aws.service.ExpirationDateHelper;
import org.opengroup.osdu.file.aws.service.StorageServiceImpl;
import org.opengroup.osdu.file.model.SignedUrl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes={FileAwsApplication.class})
public class StorageServiceImplTest {

  @InjectMocks
  private StorageServiceImpl repo;

  @Mock
  private DynamoDBQueryHelper queryHelper;

  @Mock
  private AmazonS3 s3Client;

  @Mock
  private Authorizer authorizer;

  @Mock
  private  ExpirationDateHelper expirationDateHelper;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testCreateSignedUrlForUnsignedUrl() throws IOException {
    // Arrange
    String testFileId = "testId";
    String testAuthToken = "testAuthToken";
    String testDataPartitionId = "testDataPartitionId";
    String testUrl = "http://localhost";
    String testUser = "testUser";
    String testVersionPath = "v1/";

    Whitebox.setInternalState(repo, "datafilesBucketName", "test-bucket-name");
    int expInDays = 7;
    Whitebox.setInternalState(repo, "s3SignedUrlExpirationTimeInDays", expInDays);
    Whitebox.setInternalState(repo, "s3DatafilePrefix", testVersionPath);

    java.util.Date expiration = new java.util.Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * expInDays;
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest("test-bucket-name", "testId")
            .withMethod(HttpMethod.GET)
            .withExpiration(expiration);

    URL url = new URL(testUrl);
    Mockito.when(s3Client.generatePresignedUrl(Mockito.anyObject()))
        .thenReturn(url);

    Mockito.when(authorizer.validateJWT(Mockito.eq(testAuthToken)))
        .thenReturn(testUser);

    Mockito.when(expirationDateHelper.getExpirationDate(expInDays))
        .thenReturn(expiration);

    ArgumentCaptor<GeneratePresignedUrlRequest> urlRequest = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);

    // Act
    SignedUrl signedUrl = repo.createSignedUrl("s3://test-bucket-name/unsignedurl", "testing token");

    // Assert
    Mockito.verify(s3Client, Mockito.times(1)).generatePresignedUrl(urlRequest.capture());
    Assert.assertEquals("test-bucket-name", urlRequest.getValue().getBucketName());
    Assert.assertEquals("unsignedurl", urlRequest.getValue().getKey());
    Assert.assertEquals(generatePresignedUrlRequest.getExpiration().getTime(), urlRequest.getValue().getExpiration().getTime());
  }

  @Test
  public void testCreateSignedUrl() throws IOException {
    // Arrange
    String testFileId = "testId";
    String testAuthToken = "testAuthToken";
    String testDataPartitionId = "testDataPartitionId";
    String testUrl = "http://localhost";
    String testUser = "testUser";
    String testVersionPath = "v1/";

    Whitebox.setInternalState(repo, "datafilesBucketName", "test-bucket-name");
    int expInDays = 7;
    Whitebox.setInternalState(repo, "s3SignedUrlExpirationTimeInDays", expInDays);
    Whitebox.setInternalState(repo, "s3DatafilePrefix", testVersionPath);

    java.util.Date expiration = new java.util.Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * expInDays;
    expiration.setTime(expTimeMillis);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest("test-bucket-name", "testId")
            .withMethod(HttpMethod.GET)
            .withExpiration(expiration);

    URL url = new URL(testUrl);

    Mockito.when(s3Client.generatePresignedUrl(Mockito.anyObject()))
        .thenReturn(url);

    Mockito.when(authorizer.validateJWT(Mockito.eq(testAuthToken)))
        .thenReturn(testUser);

    Mockito.when(expirationDateHelper.getExpirationDate(expInDays))
        .thenReturn(expiration);

    ArgumentCaptor<GeneratePresignedUrlRequest> urlRequest = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);

    // Act
    SignedUrl signedUrl = repo.createSignedUrl(testFileId, testAuthToken, testDataPartitionId, "PUT");

    // Assert
    Mockito.verify(s3Client, Mockito.times(1)).generatePresignedUrl(urlRequest.capture());
    Assert.assertEquals(generatePresignedUrlRequest.getBucketName(), urlRequest.getValue().getBucketName());
    Assert.assertEquals(testVersionPath + testDataPartitionId + "/" + generatePresignedUrlRequest.getKey(), urlRequest.getValue().getKey());
    Assert.assertEquals(generatePresignedUrlRequest.getExpiration().getTime(), urlRequest.getValue().getExpiration().getTime());
    Assert.assertEquals(testUser, signedUrl.getCreatedBy());
    Assert.assertEquals(testUrl, signedUrl.getUrl().toString());
  }

}
