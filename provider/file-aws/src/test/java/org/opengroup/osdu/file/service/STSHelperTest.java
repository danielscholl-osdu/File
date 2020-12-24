// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.file.service;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.file.provider.aws.config.AwsServiceConfig;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.opengroup.osdu.file.provider.aws.model.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.util.InstantHelper;
import org.opengroup.osdu.file.provider.aws.util.STSHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class STSHelperTest {

    @InjectMocks
    private STSHelper CUT;

    @Mock
    private AwsServiceConfig awsServiceConfig;

    @Mock
    private AWSSecurityTokenService sts;

    @Spy
    private InstantHelper instantHelper;

    private String roleArn = "arn:partition:service:region:account-id:resource-id";
    private String user = "admin@example.com";
    private S3Location fileLocation = new S3Location("s3://bucket/path/key");

    @BeforeEach
    public void setUp() throws Exception {
        awsServiceConfig.amazonRegion = "us-east-1";
    }

    @Test
    public void should_getFolderCredentials_for_SingleFileTypes() throws JSONException {
        String srn = "opendes:doc:a6d1cc3605e649d1a01575be868412c7";
        String expectedArn = "arn:aws:s3:::bucket";
        Instant now = instantHelper.now();
        String expectedRoleSessionName = String.format("%s_%s", user, now.toEpochMilli());
        Date expirationDate = Date.from(now.plusSeconds(3600));

        Mockito.when(instantHelper.now()).thenReturn(now);
        AssumeRoleResult mockAssumeRoleResult = Mockito.mock(AssumeRoleResult.class);
        Credentials mockCredentials = new Credentials().withAccessKeyId("AccessKeyId")
                .withSessionToken("SessionToken")
                .withSecretAccessKey("SecretAccessKey")
                .withExpiration(expirationDate);
        Mockito.when(mockAssumeRoleResult.getCredentials()).thenReturn(mockCredentials);
        Mockito.when(sts.assumeRole(Mockito.any())).thenReturn(mockAssumeRoleResult);

        TemporaryCredentials credentials =  CUT.getGetCredentials(srn, fileLocation, roleArn, user, expirationDate);
        ArgumentCaptor<AssumeRoleRequest> requestArgumentCaptor = ArgumentCaptor.forClass(AssumeRoleRequest.class);
        Mockito.verify(sts, Mockito.times(1)).assumeRole(requestArgumentCaptor.capture());
        AssumeRoleRequest assumeRoleRequest = requestArgumentCaptor.getValue();
        assertEquals(roleArn, assumeRoleRequest.getRoleArn());
        assertEquals(expectedRoleSessionName, assumeRoleRequest.getRoleSessionName());
        String policyJson = assumeRoleRequest.getPolicy();
        System.out.println(policyJson);

        String resource = getFirstResourceArn(policyJson);
        assertEquals(expectedArn, resource);
        assertEquals(mockCredentials.getAccessKeyId(), credentials.getAccessKeyId());
        assertEquals(mockCredentials.getSecretAccessKey(), credentials.getSecretAccessKey());
        assertEquals(mockCredentials.getSessionToken(), mockCredentials.getSessionToken());
    }

    @Test
    public void should_getFolderCredentials_for_OVDSTypes() throws JSONException {
        String srn = "opendes:ovds:a6d1cc3605e649d1a01575be868412c7";
        String expectedArn = "arn:aws:s3:::bucket";
        Instant now = instantHelper.now();
        String expectedRoleSessionName = String.format("%s_%s", user, now.toEpochMilli());
        Date expirationDate = Date.from(now.plusSeconds(3600));

        Mockito.when(instantHelper.now()).thenReturn(now);
        AssumeRoleResult mockAssumeRoleResult = Mockito.mock(AssumeRoleResult.class);
        Credentials mockCredentials = new Credentials().withAccessKeyId("AccessKeyId")
                .withSessionToken("SessionToken")
                .withSecretAccessKey("SecretAccessKey")
                .withExpiration(expirationDate);
        Mockito.when(mockAssumeRoleResult.getCredentials()).thenReturn(mockCredentials);
        Mockito.when(sts.assumeRole(Mockito.any())).thenReturn(mockAssumeRoleResult);

        TemporaryCredentials credentials =  CUT.getGetCredentials(srn, fileLocation, roleArn, user, expirationDate);
        ArgumentCaptor<AssumeRoleRequest> requestArgumentCaptor = ArgumentCaptor.forClass(AssumeRoleRequest.class);
        Mockito.verify(sts, Mockito.times(1)).assumeRole(requestArgumentCaptor.capture());
        AssumeRoleRequest assumeRoleRequest = requestArgumentCaptor.getValue();
        assertEquals(roleArn, assumeRoleRequest.getRoleArn());
        assertEquals(expectedRoleSessionName, assumeRoleRequest.getRoleSessionName());
        String policyJson = assumeRoleRequest.getPolicy();
        System.out.println(policyJson);

        String resource = getFirstResourceArn(policyJson);
        assertEquals(expectedArn, resource);
        assertEquals(mockCredentials.getAccessKeyId(), credentials.getAccessKeyId());
        assertEquals(mockCredentials.getSecretAccessKey(), credentials.getSecretAccessKey());
        assertEquals(mockCredentials.getSessionToken(), mockCredentials.getSessionToken());
    }

    private String getFirstResourceArn(String policyJson) throws JSONException {
        JSONObject policyObject = new JSONObject(policyJson);
        String resource = policyObject.getJSONArray("Statement")
                .getJSONObject(0).getJSONArray("Resource")
                .getString(0);
        return resource;
    }
}