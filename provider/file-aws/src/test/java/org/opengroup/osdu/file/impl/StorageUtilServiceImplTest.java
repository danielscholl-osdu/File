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

package org.opengroup.osdu.file.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.Headers;
import org.junit.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.aws.helper.ExpirationDateHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import static org.junit.Assert.assertNotNull;
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

    @InjectMocks
    private StorageUtilServiceImpl storageUtilService;

    @Test
    public void testGetChecksum() throws Exception {
        String uri = "s3://bucket/path/key";
        S3Location unsignedLocation = S3Location.of(uri);
        Date expirationDate = new Date(System.currentTimeMillis() + 3600L * 1000L);

        TemporaryCredentials credentials = mock(TemporaryCredentials.class);
        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.withCredentials(Mockito.any())).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.withRegion(Mockito.any(Regions.class))).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        Mockito.when(stsCredentialsHelper.getRetrievalCredentials(Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(credentials);
        Mockito.when(stsRoleHelper.getRoleArnForPartition(Mockito.any(DpsHeaders.class), Mockito.any()))
                .thenReturn("testRole");

        ExpiryTimeUtil expTimeUtil = new ExpiryTimeUtil();
        RelativeTimeValue relativeTimeValue = expTimeUtil.getExpiryTimeValueInTimeUnit(null);
        Mockito.when(expiryTimeUtil.getExpiryTimeValueInTimeUnit(Mockito.any())).thenReturn(relativeTimeValue);
        MockedStatic<ExpirationDateHelper> expMockedStatic = mockStatic(ExpirationDateHelper.class);
        expMockedStatic
                .when(() -> ExpirationDateHelper.getExpiration(Mockito.any(Instant.class), Mockito.any(Duration.class)))
                .thenReturn(expirationDate);

        Mockito.when(s3Mock.doesObjectExist(Mockito.any(), Mockito.any())).thenReturn(true);

        S3Object s3ObjMock = new S3Object();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader(Headers.CONTENT_LENGTH, 10L);
        s3ObjMock.setObjectMetadata(objectMetadata);
        GetObjectRequest getObjectRequest = new GetObjectRequest(unsignedLocation.getBucket(),
                unsignedLocation.getKey());
        Mockito.when(s3Mock.getObject(getObjectRequest)).thenReturn(s3ObjMock);

        byte[] testData = "test".getBytes();
        ByteArrayInputStream inputTestData = new ByteArrayInputStream(testData);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(inputTestData, null);
        s3ObjMock.setObjectContent(s3ObjectInputStream);

        String actual = storageUtilService.getChecksum(uri);
        assertNotNull(actual);

        s3ClientMock.close();
        expMockedStatic.close();
    }
}
