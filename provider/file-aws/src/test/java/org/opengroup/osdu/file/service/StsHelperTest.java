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

package org.opengroup.osdu.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.auth.TemporaryCredentials;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class StsHelperTest {

    private final String roleArn = "arn:partition:service:region:account-id:resource-id";
    private final String user = "admin@example.com";
    private final S3Location fileLocation = S3Location.of("s3://bucket/path/key");

    private StsCredentialsHelper stsCredentialsHelper;

    @Mock
    private ProviderConfigurationBag providerConfigurationBag;
    @Mock
    private AWSSecurityTokenService securityTokenService;

    @BeforeEach
    public void setUp() {
        providerConfigurationBag.amazonRegion = "us-east-1";
        stsCredentialsHelper = new StsCredentialsHelper(providerConfigurationBag);

        ReflectionTestUtils.setField(stsCredentialsHelper, "securityTokenService", securityTokenService);
    }

    @Test
    public void shouldGetFolderCredentialsForSingleFileTypes() throws JSONException {
        String expectedArn = "arn:aws:s3:::bucket";
        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plusSeconds(3600));

        AssumeRoleResult mockAssumeRoleResult = Mockito.mock(AssumeRoleResult.class);
        Credentials mockCredentials = new Credentials().withAccessKeyId("AccessKeyId")
                                                       .withSessionToken("SessionToken")
                                                       .withSecretAccessKey("SecretAccessKey")
                                                       .withExpiration(expirationDate);
        Mockito.when(mockAssumeRoleResult.getCredentials()).thenReturn(mockCredentials);
        Mockito.when(securityTokenService.assumeRole(Mockito.any())).thenReturn(mockAssumeRoleResult);

        TemporaryCredentials credentials = stsCredentialsHelper.getRetrievalCredentials(fileLocation, roleArn, user, expirationDate);

        ArgumentCaptor<AssumeRoleRequest> requestArgumentCaptor = ArgumentCaptor.forClass(AssumeRoleRequest.class);
        Mockito.verify(securityTokenService, Mockito.times(1)).assumeRole(requestArgumentCaptor.capture());
        AssumeRoleRequest assumeRoleRequest = requestArgumentCaptor.getValue();

        assertEquals(roleArn, assumeRoleRequest.getRoleArn());

        String policyJson = assumeRoleRequest.getPolicy();
        log.info("Policy: {}", policyJson);

        String resource = getFirstResourceArn(policyJson);
        assertEquals(expectedArn, resource);
        assertEquals(mockCredentials.getAccessKeyId(), credentials.getAccessKeyId());
        assertEquals(mockCredentials.getSecretAccessKey(), credentials.getSecretAccessKey());
        assertEquals(mockCredentials.getSessionToken(), mockCredentials.getSessionToken());
    }

    @Test
    public void shouldGetFolderCredentialsForOvdsTypes() throws JSONException {
        String expectedArn = "arn:aws:s3:::bucket";
        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plusSeconds(3600));

        AssumeRoleResult mockAssumeRoleResult = Mockito.mock(AssumeRoleResult.class);
        Credentials mockCredentials = new Credentials().withAccessKeyId("AccessKeyId")
                                                       .withSessionToken("SessionToken")
                                                       .withSecretAccessKey("SecretAccessKey")
                                                       .withExpiration(expirationDate);
        Mockito.when(mockAssumeRoleResult.getCredentials()).thenReturn(mockCredentials);
        Mockito.when(securityTokenService.assumeRole(Mockito.any())).thenReturn(mockAssumeRoleResult);

        TemporaryCredentials credentials = stsCredentialsHelper.getRetrievalCredentials(fileLocation, roleArn, user, expirationDate);
        ArgumentCaptor<AssumeRoleRequest> requestArgumentCaptor = ArgumentCaptor.forClass(AssumeRoleRequest.class);
        Mockito.verify(securityTokenService, Mockito.times(1)).assumeRole(requestArgumentCaptor.capture());
        AssumeRoleRequest assumeRoleRequest = requestArgumentCaptor.getValue();

        assertEquals(roleArn, assumeRoleRequest.getRoleArn());

        String policyJson = assumeRoleRequest.getPolicy();
        log.info("Policy: {}", policyJson);

        String resource = getFirstResourceArn(policyJson);
        assertEquals(expectedArn, resource);
        assertEquals(mockCredentials.getAccessKeyId(), credentials.getAccessKeyId());
        assertEquals(mockCredentials.getSecretAccessKey(), credentials.getSecretAccessKey());
        assertEquals(mockCredentials.getSessionToken(), mockCredentials.getSessionToken());
    }

    private String getFirstResourceArn(String policyJson) throws JSONException {
        JSONObject policyObject = new JSONObject(policyJson);
        return policyObject.getJSONArray("Statement")
                           .getJSONObject(0).getJSONArray("Resource")
                           .getString(0);
    }
}
