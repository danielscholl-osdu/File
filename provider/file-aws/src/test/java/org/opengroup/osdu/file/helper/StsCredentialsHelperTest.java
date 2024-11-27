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

package org.opengroup.osdu.file.helper;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.sts.STSConfig;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;

public class StsCredentialsHelperTest {

    private static final String TEST_REGION = "us-east-1";
    private static final String TEST_BUCKET = "XXXXXX";
    private static final String TEST_KEY = "key";
    private static final String TEST_ROLE_ARN = "roleArn";
    private static final Date TEST_EXPIRATION = new Date(2040, Calendar.JANUARY, 1, 1, 0, 0);

    @Mock
    private AWSSecurityTokenService securityTokenService;

    @Mock
    private AssumeRoleResult assumeRoleResult;

    @Mock
    private Credentials credentials;

    private ProviderConfigurationBag providerConfigurationBag;
    private StsCredentialsHelper stsCredentialsHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        providerConfigurationBag = new ProviderConfigurationBag();
        providerConfigurationBag.amazonRegion = TEST_REGION;

        // Setup default mock behavior
        when(assumeRoleResult.getCredentials()).thenReturn(credentials);
        when(securityTokenService.assumeRole(any(AssumeRoleRequest.class))).thenReturn(assumeRoleResult);
    }

    @Test
    public void shouldGetUploadCredentials_WhenValidParametersProvided() {
        // Arrange
        try (MockedConstruction<STSConfig> config = Mockito.mockConstruction(STSConfig.class,
            (mock, context) -> when(mock.amazonSTS()).thenReturn(securityTokenService))) {

            stsCredentialsHelper = new StsCredentialsHelper(providerConfigurationBag);
            S3Location s3Location = new S3Location(TEST_BUCKET, TEST_KEY);

            // Act
            Object result = stsCredentialsHelper.getUploadCredentials(s3Location, TEST_ROLE_ARN, TEST_EXPIRATION);

            // Assert
            assertNotNull("Upload credentials should not be null", result);
            verify(securityTokenService).assumeRole(any(AssumeRoleRequest.class));
            verify(assumeRoleResult).getCredentials();
        }
    }

    @Test(expected = OsduBadRequestException.class)
    public void shouldThrowRuntimeException_WhenSecurityTokenServiceFails() {
        // Arrange
        when(securityTokenService.assumeRole(any(AssumeRoleRequest.class)))
            .thenThrow(new AWSSecurityTokenServiceException("Failed to assume role"));

        try (MockedConstruction<STSConfig> config = Mockito.mockConstruction(STSConfig.class,
            (mock, context) -> when(mock.amazonSTS()).thenReturn(securityTokenService))) {

            stsCredentialsHelper = new StsCredentialsHelper(providerConfigurationBag);
            S3Location s3Location = new S3Location(TEST_BUCKET, TEST_KEY);

            // Act
            stsCredentialsHelper.getUploadCredentials(s3Location, TEST_ROLE_ARN, TEST_EXPIRATION);
        }
    }

    @Test
    public void shouldRoundUpToOneMinute_WhenDurationIsLessThanOneMinute() {
        // Arrange
        try (MockedConstruction<STSConfig> config = Mockito.mockConstruction(STSConfig.class,
            (mock, context) -> when(mock.amazonSTS()).thenReturn(securityTokenService))) {

            stsCredentialsHelper = new StsCredentialsHelper(providerConfigurationBag);
            S3Location s3Location = new S3Location(TEST_BUCKET, TEST_KEY);

            Date now = new Date();
            // Set expiration to 45 seconds
            Date expiration = new Date(now.getTime() + 45_000);

            // Act
            stsCredentialsHelper.getUploadCredentials(s3Location, TEST_ROLE_ARN, expiration);

            // Assert
            verify(securityTokenService).assumeRole(argThat(request ->
                request.getDurationSeconds() == 60));
        }
    }
}

