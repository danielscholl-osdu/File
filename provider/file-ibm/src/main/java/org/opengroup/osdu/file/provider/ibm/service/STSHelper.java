package org.opengroup.osdu.file.provider.ibm.service;

import java.time.Instant;
import java.util.Date;

import javax.inject.Inject;

import org.opengroup.osdu.file.provider.ibm.model.file.S3Location;
import org.opengroup.osdu.file.provider.ibm.model.file.TemporaryCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import lombok.extern.slf4j.Slf4j;

@Component
public class STSHelper {
	
	
	private static final Integer MAX_DURATION_IN_SECONDS = 3600;
	
	@Inject
	private InstantHelper instantHelper;
	
	@Value("${ibm.cos.endpoint_url}")
	private String endpointurl;
	
	@Value("${ibm.cos.region:us-south}")
	private String region;
	
	@Value("${ibm.cos.subuser}")
	private String subuser;
	
	@Value("${ibm.cos.subpassword}")
	private String subpassword;
	
	public TemporaryCredentials getUploadCredentials(S3Location fileLocation,
		    String roleArn, String user, Date expiration) {
			Policy policy = createUploadPolicy(fileLocation);
		       return getCredentials(policy, roleArn, user, expiration);

		  }
	
	public TemporaryCredentials getRetrievalCredentials(S3Location fileLocation,
		    String roleArn, String user, Date expiration) {

		      Policy policy = createRetrievalPolicy(fileLocation);
		      
		   return getCredentials(policy, roleArn, user, expiration);
		  
		  }
	
	private TemporaryCredentials getCredentials(Policy policy,
		    String roleArn, String user, Date expiration) {
		    Instant now = instantHelper.getCurrentInstant();
		    String roleSessionName = String.format("%s_%s", user, now.toEpochMilli());

		    Long duration = ((expiration.getTime() - now.toEpochMilli()) / 1000);
		    duration = duration > MAX_DURATION_IN_SECONDS ? MAX_DURATION_IN_SECONDS : duration;

		    AssumeRoleRequest roleRequest = new AssumeRoleRequest()
		            .withRoleArn(roleArn)
		            .withRoleSessionName(roleSessionName)
		            .withDurationSeconds(duration.intValue())
		            .withPolicy(policy.toJson());
		    AWSCredentials credentials = new BasicAWSCredentials(subuser,
	    			 subpassword); 
	    	 ClientConfiguration clientConfiguration = new ClientConfiguration();
		    
		    AWSSecurityTokenService stsClient =
	                 AWSSecurityTokenServiceClientBuilder.standard()
	                 .withEndpointConfiguration(new
	                 AwsClientBuilder.EndpointConfiguration("https://"+endpointurl, region))
	                 .withClientConfiguration(clientConfiguration) .withCredentials(new
	                 AWSStaticCredentialsProvider(credentials)) .build();

		    AssumeRoleResult response = stsClient.assumeRole(roleRequest);

		    Credentials sessionCredentials = response.getCredentials();

		    TemporaryCredentials temporaryCredentials = TemporaryCredentials
		            .builder()
		            .accessKeyId(sessionCredentials.getAccessKeyId())
		            .expiration(sessionCredentials.getExpiration())
		            .secretAccessKey(sessionCredentials.getSecretAccessKey())
		            .sessionToken(sessionCredentials.getSessionToken())
		            .build();

		    return temporaryCredentials;
		  }
	
	private Policy createRetrievalPolicy(S3Location fileLocation) {
		
		//Some string formats below assume no trailing slash.
	    String fileLocationKey = fileLocation.key;
	    String fileLocationKeyWithoutTrailingSlash = fileLocation.key.replaceFirst("/$", "");
	    	    
	    Policy policy = new Policy();
	    
	    Statement listBucketStatement = new Statement(Statement.Effect.Allow);
	    String resource = String.format("arn:aws:s3:::%s", fileLocation.bucket);  
	    Condition condition = new Condition().withType("StringEquals").withConditionKey("s3:prefix").withValues(fileLocationKey);
	    listBucketStatement = listBucketStatement
	    	      .withResources(new Resource(resource))
	    	      .withConditions(condition)
	    	      .withActions(S3Actions.ListObjects);
	    	      
	    	    //Statement 2: Allow Listing files under the file location
	    	    Statement listBucketSubpathStatement = new Statement(Statement.Effect.Allow);
	    	    String resource2 = String.format("arn:aws:s3:::%s", fileLocation.bucket);    
	    	    Condition condition2 = new Condition()
	    	      .withType("StringLike")
	    	      .withConditionKey("s3:prefix")
	    	      .withValues(String.format("%s/*", fileLocationKeyWithoutTrailingSlash));

	    	    listBucketSubpathStatement = listBucketSubpathStatement
	    	        .withResources(new Resource(resource2))
	    	        .withConditions(condition2)
	    	        .withActions(S3Actions.AllS3Actions);

	    	    //Statement 3: Allow Downloading files at the file location
	    	    Statement AllowDownloadStatement = new Statement(Statement.Effect.Allow);
	    	    String resource3 = String.format("arn:aws:s3:::%s/%s", fileLocation.bucket, fileLocationKey);    
	    	    
	    	    AllowDownloadStatement = AllowDownloadStatement
	    	        .withResources(new Resource(resource3))        
	    	        .withActions(S3Actions.GetObject, S3Actions.GetBucketLocation);

	    	    //Statement 4: Allow Downloading files under the file location
	    	    Statement AllowDownloadSubpathStatement = new Statement(Statement.Effect.Allow);
	    	    String resource4 = String.format("arn:aws:s3:::%s/%s/*", fileLocation.bucket, fileLocationKeyWithoutTrailingSlash);    
	    	    
	    	    AllowDownloadSubpathStatement = AllowDownloadSubpathStatement
	    	        .withResources(new Resource(resource4))        
	    	        .withActions(S3Actions.GetObject, S3Actions.GetBucketLocation);

	    	    return policy.withStatements(listBucketStatement, listBucketSubpathStatement, AllowDownloadStatement, AllowDownloadSubpathStatement);

	}
	
	private Policy createUploadPolicy(S3Location fileLocation) {

	    //Some string formats below assume no trailing slash.  
	    String fileLocationKey = fileLocation.key;
	    String fileLocationKeyWithoutTrailingSlash = fileLocation.key.replaceFirst("/$", "");

	    Policy policy = new Policy();

	    //Statement 1: Allow Listing files at the file location
	    Statement listBucketStatement = new Statement(Statement.Effect.Allow);
	    String resource = String.format("arn:aws:s3:::%s", fileLocation.bucket);    
	    
	    listBucketStatement = listBucketStatement
	      .withResources(new Resource(resource))
	      .withActions(S3Actions.ListObjects);

	    //Statement 2: Allow Listing files under the file location
	    Statement listBucketSubpathStatement = new Statement(Statement.Effect.Allow);
	    String resource2 = String.format("arn:aws:s3:::%s", fileLocation.bucket);    

	    listBucketSubpathStatement = listBucketSubpathStatement
	        .withResources(new Resource(resource2))
	        .withActions(S3Actions.AllS3Actions);    

	    
	    //Statement 3: Allow Uploading files at the file location
	    Statement AllowUploadStatement = new Statement(Statement.Effect.Allow);
	    String resource3 = String.format("arn:aws:s3:::%s/%s", fileLocation.bucket, fileLocationKey);    
	    
	    AllowUploadStatement = AllowUploadStatement
	        .withResources(new Resource(resource3))        
	        .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads, 
	          S3Actions.ListMultipartUploadParts, S3Actions.AbortMultipartUpload);

	    //Statement 4: Allow Uploading files under the file location
	    Statement AllowUploadSubpathStatement = new Statement(Statement.Effect.Allow);
	    String resource4 = String.format("arn:aws:s3:::%s/%s/*", fileLocation.bucket, fileLocationKeyWithoutTrailingSlash);
	    
	    AllowUploadSubpathStatement = AllowUploadSubpathStatement
	        .withResources(new Resource(resource4))        
	        .withActions(S3Actions.PutObject, S3Actions.ListBucketMultipartUploads, 
	          S3Actions.ListMultipartUploadParts, S3Actions.AbortMultipartUpload);

	    return policy.withStatements(listBucketStatement, listBucketSubpathStatement, AllowUploadStatement, AllowUploadSubpathStatement);

	  }


}