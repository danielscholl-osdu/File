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

package org.opengroup.osdu.file.impl.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.status.StatusDetails;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.impl.status.StatusEventPublisherImpl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;

@RunWith(MockitoJUnitRunner.class)

public class StatusEventPublisherImplTest {

    private final String amazonSnsTopic = "amazonSnsTopic";
    private final String region = "us-east-1";


    @Test(expected = CoreException.class)
    public void testPublish_invalidMessages() {
        AmazonSNS snsClient = mock(AmazonSNS.class);

        try (MockedConstruction<K8sLocalParameterProvider> provider = Mockito.mockConstruction(K8sLocalParameterProvider.class, (mock, context) -> {
            when(mock.getParameterAsStringOrDefault(anyString(), any())).thenReturn(amazonSnsTopic);
            })) {
                try (MockedConstruction<AmazonSNSConfig> config = Mockito.mockConstruction(AmazonSNSConfig.class, (mock1, context) -> {
                    when(mock1.AmazonSNS()).thenReturn(snsClient);
                    })) {

                        Message[] messages = new Message[0];
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                        providerConfigurationBag.amazonSnsRegion = region;

                        StatusEventPublisherImpl publisher = new StatusEventPublisherImpl(providerConfigurationBag);
                        publisher.publish(messages, attributesMap);
                    }

            }
    }

    @Test(expected = CoreException.class)
    public void testPublish_emptyAttributes() {
        AmazonSNS snsClient = mock(AmazonSNS.class);

        try (MockedConstruction<K8sLocalParameterProvider> provider = Mockito.mockConstruction(K8sLocalParameterProvider.class, (mock, context) -> {
            when(mock.getParameterAsStringOrDefault(anyString(), any())).thenReturn(amazonSnsTopic);
            })) {
                try (MockedConstruction<AmazonSNSConfig> config = Mockito.mockConstruction(AmazonSNSConfig.class, (mock1, context) -> {
                    when(mock1.AmazonSNS()).thenReturn(snsClient);
                    })) {

                        Message[] messages = new Message[1];
                        messages[0] = new StatusDetails();
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                        providerConfigurationBag.amazonSnsRegion = region;

                        StatusEventPublisherImpl publisher = new StatusEventPublisherImpl(providerConfigurationBag);
                        publisher.publish(messages, attributesMap);
                    }

            }
    }

    @Test(expected = CoreException.class)
    public void testPublish_noPartitionID() {
        AmazonSNS snsClient = mock(AmazonSNS.class);

        try (MockedConstruction<K8sLocalParameterProvider> provider = Mockito.mockConstruction(K8sLocalParameterProvider.class, (mock, context) -> {
            when(mock.getParameterAsStringOrDefault(anyString(), any())).thenReturn(amazonSnsTopic);
            })) {
                try (MockedConstruction<AmazonSNSConfig> config = Mockito.mockConstruction(AmazonSNSConfig.class, (mock1, context) -> {
                    when(mock1.AmazonSNS()).thenReturn(snsClient);
                    })) {

                        Message[] messages = new Message[1];
                        messages[0] = new StatusDetails();
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        attributesMap.put(DpsHeaders.CORRELATION_ID, DpsHeaders.CORRELATION_ID);
                        ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                        providerConfigurationBag.amazonSnsRegion = region;

                        StatusEventPublisherImpl publisher = new StatusEventPublisherImpl(providerConfigurationBag);
                        publisher.publish(messages, attributesMap);
                    }

            }
    }

    @Test(expected = CoreException.class)
    public void testPublish_noCorrelationId() {
        AmazonSNS snsClient = mock(AmazonSNS.class);

        try (MockedConstruction<K8sLocalParameterProvider> provider = Mockito.mockConstruction(K8sLocalParameterProvider.class, (mock, context) -> {
            when(mock.getParameterAsStringOrDefault(anyString(), any())).thenReturn(amazonSnsTopic);
            })) {
                try (MockedConstruction<AmazonSNSConfig> config = Mockito.mockConstruction(AmazonSNSConfig.class, (mock1, context) -> {
                    when(mock1.AmazonSNS()).thenReturn(snsClient);
                    })) {

                        Message[] messages = new Message[1];
                        messages[0] = new StatusDetails();
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        attributesMap.put(DpsHeaders.DATA_PARTITION_ID, DpsHeaders.DATA_PARTITION_ID);
                        ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                        providerConfigurationBag.amazonSnsRegion = region;

                        StatusEventPublisherImpl publisher = new StatusEventPublisherImpl(providerConfigurationBag);
                        publisher.publish(messages, attributesMap);
                    }

            }
    }

    @Test
    public void testPublish() {

        AmazonSNS snsClient = mock(AmazonSNS.class);

        try (MockedConstruction<K8sLocalParameterProvider> provider = Mockito.mockConstruction(K8sLocalParameterProvider.class, (mock, context) -> {
            when(mock.getParameterAsStringOrDefault(anyString(), any())).thenReturn(amazonSnsTopic);
            })) {
                try (MockedConstruction<AmazonSNSConfig> config = Mockito.mockConstruction(AmazonSNSConfig.class, (mock1, context) -> {
                    when(mock1.AmazonSNS()).thenReturn(snsClient);
                    })) {

                        Message[] messages = new Message[1];
                        messages[0] = new StatusDetails();
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        attributesMap.put(DpsHeaders.DATA_PARTITION_ID, DpsHeaders.DATA_PARTITION_ID);
                        attributesMap.put(DpsHeaders.CORRELATION_ID, DpsHeaders.CORRELATION_ID);
                        ProviderConfigurationBag providerConfigurationBag = new ProviderConfigurationBag();
                        providerConfigurationBag.amazonSnsRegion = region;

                        StatusEventPublisherImpl publisher = new StatusEventPublisherImpl(providerConfigurationBag);
                        publisher.publish(messages, attributesMap);

                        verify(snsClient, times(1)).publish(any(PublishRequest.class));
                    }

            }
    }
    
}
