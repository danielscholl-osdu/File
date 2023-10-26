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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.opengroup.osdu.core.aws.sts.STSConfig;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.helper.StsCredentialsHelper;
import org.opengroup.osdu.file.provider.aws.model.S3Location;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

public class StsCredentialsHelperTest {

    @Test
    public void getUploadCredentials() {

        AWSSecurityTokenService securityTokenService = mock(AWSSecurityTokenService.class);
        AssumeRoleResult response = mock(AssumeRoleResult.class);

        try (MockedConstruction<STSConfig> config = Mockito.mockConstruction(STSConfig.class, (mock, context) -> {
            when(mock.amazonSTS()).thenReturn(securityTokenService);
            })) {
                ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                providerConfigurationBag.amazonRegion = "us-east-1";

                when(securityTokenService.assumeRole(any(AssumeRoleRequest.class))).thenReturn(response);
                when(response.getCredentials()).thenReturn(new Credentials());

                StsCredentialsHelper helper = new StsCredentialsHelper(providerConfigurationBag);

                assertNotNull(helper.getUploadCredentials(new S3Location("bucket", "key"), "roleArn", new Date(2040, 1 ,1 ,1, 0, 0)));
    
            }
    }
}
