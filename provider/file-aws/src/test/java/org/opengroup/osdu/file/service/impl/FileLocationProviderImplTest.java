// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.s3.util.S3ClientConnectionInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.helper.S3ConnectionInfoHelper;
import org.opengroup.osdu.file.provider.aws.helper.S3Helper;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.helper.StsRoleHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.service.impl.FileLocationProviderImpl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;


@RunWith(MockitoJUnitRunner.class)

public class FileLocationProviderImplTest  {

    private final String fileID = "fileID";
    private final String datasetID = "datasetID";
    private final String partitionID = "partitionID";
    private final String bucketName = "bucketName";
    private final String keyName = "keyName";
    private final String region = "us-east-1";
    private final String endpoint = "endpoint";
    private final String bucketParameterRelativePath = "bucketParameterRelativePath";
    private final String stsRoleIamParameterRelativePath = "stsRoleIamParameterRelativePath";
    private final String stsRoleArn = "stsRoleArn";
    private final String localhost = "http://localhost";
    private final String badURL = "http://localhost^bad";

    @Mock
    ProviderConfigurationBag providerConfigurationBag;

    @Mock
    DpsHeaders headers;

    @Mock
    StsRoleHelper stsRoleHelper;

    @Mock
    StsCredentialsHelper stsCredentialsHelper;

    @Mock
    S3ConnectionInfoHelper s3ConnectionInfoHelper;

    @Mock
    ExpirationDateHelper expirationDateHelper;

	@Mock
    S3Helper s3Helper;

    AmazonS3 s3Mock = mock(AmazonS3.class);
    AmazonS3ClientBuilder mocks3Builder = mock(AmazonS3ClientBuilder.class);
    MockedStatic<S3Helper> mockS3Helper;

    @InjectMocks
    private FileLocationProviderImpl provider;

    @Before
	public void setUp() throws Exception {
        mockS3Helper = mockStatic(S3Helper.class);
	}

	@After
	public void after() {
        mockS3Helper.close();
	}	

    @Test(expected = AppException.class)
    public void testGetUploadFileLocation_nullS3ConnectionInfo() {

        provider.getUploadFileLocation(fileID, partitionID);

    }

    @Test(expected = AppException.class)
    public void testGetUploadFileLocation_nullStsRoleArn() {

        providerConfigurationBag.bucketParameterRelativePath = bucketParameterRelativePath;
        when(s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(any(DpsHeaders.class), anyString())).thenReturn(new S3ClientConnectionInfo(bucketName, region, endpoint));

        provider.getUploadFileLocation(fileID, partitionID);

    }

    

    @Test
    public void testGetUploadLocation() throws MalformedURLException {

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        providerConfigurationBag.bucketParameterRelativePath = bucketParameterRelativePath;
        when(s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(any(DpsHeaders.class), anyString())).thenReturn(new S3ClientConnectionInfo(bucketName, region, endpoint));
        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getUploadCredentials(any(), anyString(), any(), any())).thenReturn(credentials);

        mockS3Helper.when(() -> S3Helper.generatePresignedUrl(any(), any(), any(), any())).thenReturn(new URL(localhost));

        assertNotNull(provider.getUploadFileLocation(fileID, partitionID));
        assertNotNull(provider.getFileCollectionUploadLocation(datasetID, partitionID));

    }

    @Test(expected = AppException.class)
    public void testGetUploadFileLocation_badURI() throws MalformedURLException {

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        providerConfigurationBag.bucketParameterRelativePath = bucketParameterRelativePath;
        when(s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(any(DpsHeaders.class), anyString())).thenReturn(new S3ClientConnectionInfo(bucketName, region, endpoint));
        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getUploadCredentials(any(), anyString(), any(), any())).thenReturn(credentials);

        mockS3Helper.when(() -> S3Helper.generatePresignedUrl(any(), any(), any(), any())).thenReturn(new URL(badURL));

        provider.getUploadFileLocation(fileID, partitionID);

    }

    /**@Test(expected = IllegalArgumentException.class)
    public void testGetUploadFileLocation_longObjectKey() throws MalformedURLException {

        MockedStatic<S3Location> mockS3Location = mockStatic(S3Location.class);

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        //FileLocationProviderImpl provider = new FileLocationProviderImpl(providerConfigurationBag, stsCredentialsHelper, stsRoleHelper, s3ConnectionInfoHelper, headers);
        providerConfigurationBag.bucketParameterRelativePath = bucketParameterRelativePath;
        when(s3ConnectionInfoHelper.getS3ConnectionInfoForPartition(any(DpsHeaders.class), anyString())).thenReturn(new S3ClientConnectionInfo(bucketName, region, endpoint));
        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getUploadCredentials(any(), anyString(), any(), any())).thenReturn(credentials);
        
        S3LocationBuilder s3LocationBuilder = mock(S3LocationBuilder.class);
        S3Location unsignedLocation = mock(S3Location.class);
        when(unsignedLocation.getKey()).thenReturn(new String(new char[1025]).replace('\0', ' '));
        when(s3LocationBuilder.build()).thenReturn(unsignedLocation);
        when(s3LocationBuilder.withBucket(anyString())).thenReturn(s3LocationBuilder);
        when(s3LocationBuilder.withFolder(anyString())).thenReturn(s3LocationBuilder);

        mockS3Location.when(() -> S3Location.newBuilder()).thenReturn(s3LocationBuilder);

        provider.getUploadFileLocation(fileID, partitionID);
        mockS3Location.close();

    }*/

    @Test(expected = AppException.class)
    public void testGetRetrievalFileLocation_nullStsRoleArn() {
        provider.getRetrievalFileLocation(new S3Location(bucketName, keyName), Duration.ofSeconds(1800));
    }

    @Test
    public void testGetRetrievalLocation() throws MalformedURLException {

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getRetrievalCredentials(any(), anyString(), any(), any())).thenReturn(credentials);

        mockS3Helper.when(() -> S3Helper.doesObjectExist(any(), any())).thenReturn(true);
        mockS3Helper.when(() -> S3Helper.doesObjectCollectionExist(any(), any())).thenReturn(true);
        mockS3Helper.when(() -> S3Helper.generatePresignedUrl(any(), any(), any(), any())).thenReturn(new URL(localhost));

        assertNotNull(provider.getRetrievalFileLocation(new S3Location(bucketName, keyName), Duration.ofSeconds(1800)));
        assertNotNull(provider.getFileCollectionRetrievalLocation(new S3Location(bucketName, keyName + "/"), Duration.ofSeconds(1800)));
    }

    @Test
    public void testGetRetrievalFileLocation_responseHeaderOverrides() throws MalformedURLException {

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getRetrievalCredentials(any(), anyString(), any(), any())).thenReturn(credentials);

        mockS3Helper.when(() -> S3Helper.doesObjectExist(any(), any())).thenReturn(true);
        mockS3Helper.when(() -> S3Helper.generatePresignedUrl(any(), any(), any(), any(), any())).thenReturn(new URL(localhost));

        assertNotNull(provider.getRetrievalFileLocation(new S3Location(bucketName, keyName), Duration.ofSeconds(1800), new ResponseHeaderOverrides()));
    }

    @Test(expected = AppException.class)
    public void testGetRetrievalFileLocation_badURI() throws MalformedURLException {

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        providerConfigurationBag.stsRoleIamParameterRelativePath = stsRoleIamParameterRelativePath;
        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), anyString())).thenReturn(stsRoleArn);
        when(stsCredentialsHelper.getRetrievalCredentials(any(), anyString(), any(), any())).thenReturn(credentials);

        mockS3Helper.when(() -> S3Helper.doesObjectExist(any(), any())).thenReturn(true);
        mockS3Helper.when(() -> S3Helper.generatePresignedUrl(any(), any(), any(), any())).thenReturn(new URL(badURL));

        provider.getRetrievalFileLocation(new S3Location(bucketName, keyName), Duration.ofSeconds(1800));
    }

    @Test(expected = AppException.class)
    public void validateInput_invalidObjectKey() {

        S3Location unsignedLocation = mock(S3Location.class);

        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), any())).thenReturn(stsRoleArn);
        when(unsignedLocation.isValid()).thenReturn(true);
        when(unsignedLocation.isFolder()).thenReturn(true);

        provider.getRetrievalFileLocation(unsignedLocation, Duration.ofSeconds(1800));
    }

    @Test(expected = AppException.class)
    public void validateInput_FileCollection_invalidObjectKey() {

        S3Location unsignedLocation = mock(S3Location.class);

        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), any())).thenReturn(stsRoleArn);
        when(unsignedLocation.isValid()).thenReturn(true);
        when(unsignedLocation.isFile()).thenReturn(true);

        provider.getFileCollectionRetrievalLocation(unsignedLocation, Duration.ofSeconds(1800));
    }

    @Test(expected = AppException.class)
    public void validateInput_fileNotExist() {

        S3Location unsignedLocation = mock(S3Location.class);

        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), any())).thenReturn(stsRoleArn);
        when(unsignedLocation.isValid()).thenReturn(true);
        when(unsignedLocation.isFolder()).thenReturn(false);
        mockS3Helper.when(() -> S3Helper.doesObjectExist(any(), any())).thenReturn(false);

        provider.getRetrievalFileLocation(unsignedLocation, Duration.ofSeconds(1800));
    }

    @Test(expected = AppException.class)
    public void validateInput_folderNotExist() {

        S3Location unsignedLocation = mock(S3Location.class);

        when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), any())).thenReturn(stsRoleArn);
        when(unsignedLocation.isValid()).thenReturn(true);
        mockS3Helper.when(() -> S3Helper.doesObjectCollectionExist(any(), any())).thenReturn(false);

        provider.getFileCollectionRetrievalLocation(unsignedLocation, Duration.ofSeconds(1800));
    }
    
}
