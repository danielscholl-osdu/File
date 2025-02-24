/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.file.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.Headers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.aws.helper.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.impl.StorageUtilServiceImpl;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.helper.StsRoleHelper;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.opengroup.osdu.file.util.ExpiryTimeUtil.RelativeTimeValue;

@RunWith(MockitoJUnitRunner.class)

public class StorageUtilServiceImplTest {

    @Mock
    StsCredentialsHelper stsCredentialsHelper;

    @Mock
    StsRoleHelper stsRoleHelper;

    @Mock
    DpsHeaders headers;

    @Mock
    ProviderConfigurationBag providerConfigurationBag;

    @Mock
    ExpiryTimeUtil expiryTimeUtil;

    @Mock
    ExpirationDateHelper expirationDateHelper;

    AmazonS3 s3Mock = mock(AmazonS3.class);
    AmazonS3ClientBuilder mocks3Builder = mock(AmazonS3ClientBuilder.class);
    MockedStatic<AmazonS3ClientBuilder> s3ClientMock = mockStatic(AmazonS3ClientBuilder.class);
    MockedStatic<ExpirationDateHelper> expMockedStatic = mockStatic(ExpirationDateHelper.class);
    S3Object s3ObjMock = new S3Object();

    @InjectMocks
    private StorageUtilServiceImpl storageUtilService;

    @Before
    public void setUp() {
        Date expirationDate = new Date(System.currentTimeMillis() + 3600L * 1000L);

        TemporaryCredentials credentials = mock(TemporaryCredentials.class);
        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.withCredentials(any())).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withRegion(any(Regions.class))).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        Mockito.when(stsCredentialsHelper.getRetrievalCredentials(any(), any(),
            any())).thenReturn(credentials);
        Mockito.when(stsRoleHelper.getRoleArnForPartition(any(DpsHeaders.class), any()))
            .thenReturn("testRole");

        ExpiryTimeUtil expTimeUtil = new ExpiryTimeUtil();
        RelativeTimeValue relativeTimeValue = expTimeUtil.getExpiryTimeValueInTimeUnit(null);
        Mockito.when(expiryTimeUtil.getExpiryTimeValueInTimeUnit(any())).thenReturn(relativeTimeValue);

        expMockedStatic
            .when(() -> ExpirationDateHelper.getExpiration(any(Instant.class), any(Duration.class)))
            .thenReturn(expirationDate);

        Mockito.when(s3Mock.doesObjectExist(any(), any())).thenReturn(true);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader(Headers.CONTENT_LENGTH, 10L);
        s3ObjMock.setObjectMetadata(objectMetadata);

        byte[] testData = "test".getBytes();
        ByteArrayInputStream inputTestData = new ByteArrayInputStream(testData);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputTestData, null);
        s3ObjMock.setObjectContent(s3ObjectInputStream);

        Mockito.lenient().when(s3Mock.getObject(any(GetObjectRequest.class))).thenReturn(s3ObjMock);
    }

    @Test
    public void testGetChecksum() {
        String uri = "s3://bucket/path/key";

        String actual = storageUtilService.getChecksum(uri);
        assertNotNull(actual);
    }

    @Test
    public void getChecksum_InvalidS3Location_ThrowsOsduBadRequestException() {
        String invalidS3Path = "invalid-s3-path";

        OsduBadRequestException exception = assertThrows(
            OsduBadRequestException.class,
            () -> storageUtilService.getChecksum(invalidS3Path)
        );

        assertTrue(exception.getMessage().contains("Invalid source file path to copy from " + invalidS3Path));

    }

    @After
    public void tearDown() {
        s3ClientMock.close();
        expMockedStatic.close();
    }
}
