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

package org.opengroup.osdu.file.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentialsProvider;
import org.opengroup.osdu.file.provider.aws.helper.S3Helper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class S3HelperTest {

    AmazonS3 s3Mock = mock(AmazonS3.class);
    AmazonS3ClientBuilder mocks3Builder = mock(AmazonS3ClientBuilder.class);
    MockedStatic<AmazonS3ClientBuilder> s3ClientMock = mockStatic(AmazonS3ClientBuilder.class);

    TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
            new Date(System.currentTimeMillis() + 3600L * 1000L));
    String uri = "s3://my-bucket/my-key";
    S3Location location = S3Location.of(uri);

    @Spy
    private List<S3ObjectSummary> s3ObjectSummary = new ArrayList<>();

    @Test
    public void testDoesObjectExist() throws Exception {

        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.withCredentials(Mockito.any())).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.withRegion(Mockito.any(Regions.class))).thenReturn(mocks3Builder);

        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        Mockito.when(s3Mock.getBucketLocation(Mockito.anyString())).thenReturn("us-east-1");

        Mockito.when(s3Mock.doesObjectExist(Mockito.any(), Mockito.any())).thenReturn(true);

        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));

        boolean actual = S3Helper.doesObjectExist(location, credentials);
        assertTrue(actual);

        Mockito.when(s3Mock.doesObjectExist(Mockito.any(), Mockito.any())).thenReturn(false);
        actual = S3Helper.doesObjectExist(location, credentials);
        assertFalse(actual);

        Mockito.when(s3Mock.doesObjectExist(Mockito.any(), Mockito.any())).thenThrow(AmazonServiceException.class);
        actual = S3Helper.doesObjectExist(location, credentials);
        assertFalse(actual);

        s3ClientMock.close();
    }

    @Test
    public void testDoesObjectCollectionExist() throws Exception {

        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withCredentials(Mockito.any())).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withRegion(Mockito.any(Regions.class))).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        Mockito.when(s3Mock.getBucketLocation(Mockito.anyString())).thenReturn("us-east-1");
        ObjectListing objectListingMock = mock(ObjectListing.class);

        Mockito.when(s3Mock.listObjects(Mockito.any(), Mockito.any())).thenReturn(objectListingMock);

        Mockito.when(objectListingMock.getObjectSummaries()).thenReturn(s3ObjectSummary);

        Mockito.when(!s3ObjectSummary.isEmpty()).thenReturn(true);

        boolean actual = S3Helper.doesObjectCollectionExist(location, credentials);
        assertFalse(actual);

        Mockito.when(!s3ObjectSummary.isEmpty()).thenReturn(false);
        actual = S3Helper.doesObjectCollectionExist(location, credentials);
        assertTrue(actual);
        s3ClientMock.close();

    }

    @Test
    public void testGetBucketRegion() throws Exception {
        TemporaryCredentials credentials = new TemporaryCredentials("accessKey", "secretKey", "sessionToken",
                new Date(System.currentTimeMillis() + 3600L * 1000L));
        String expectedRegion = "us-east-1";
        String bucketName = "my-bucket";
        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withCredentials(Mockito.any(TemporaryCredentialsProvider.class)))
                .thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        Mockito.when(s3Mock.getBucketLocation(Mockito.anyString())).thenReturn(expectedRegion);
        String actual = S3Helper.getBucketRegion(bucketName, credentials);
        assertEquals(expectedRegion, actual);
        s3ClientMock.close();
    }

    @Test
    public void testGetObject() throws Exception {

        s3ClientMock.when(AmazonS3ClientBuilder::standard).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withCredentials(Mockito.any())).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.withRegion(Mockito.any(Regions.class))).thenReturn(mocks3Builder);
        Mockito.when(mocks3Builder.build()).thenReturn(s3Mock);

        GetObjectRequest getObjectRequestMock = new GetObjectRequest(location.getBucket(), location.getKey());
        S3Object s3ObjMock = mock(S3Object.class);

        Mockito.when(s3Mock.getObject(getObjectRequestMock)).thenReturn(s3ObjMock);

        S3Object actual = S3Helper.getObject(location, credentials);
        assertEquals(s3ObjMock, actual);
        s3ClientMock.close();

    }

}
