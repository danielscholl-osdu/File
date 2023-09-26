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

package org.opengroup.osdu.file.provider.aws.impl.status;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatusEventPublisherImpl implements IEventPublisher {

    private AmazonSNS snsClient;
    private String amazonSnsTopic;

    @Value("${OSDU_TOPIC}")
    private String osduFileTopic;

    @Autowired
    public StatusEventPublisherImpl(ProviderConfigurationBag providerConfigurationBag) {
        AmazonSNSConfig snsConfig = new AmazonSNSConfig(providerConfigurationBag.amazonSnsRegion);
        snsClient = snsConfig.AmazonSNS();
        K8sLocalParameterProvider provider = new K8sLocalParameterProvider();
        amazonSnsTopic = Objects.requireNonNull(provider.getParameterAsStringOrDefault("FILE_SNS_ARN", null));
    }

    @Override
    public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
        PublishRequestBuilder<Message> publishRequestBuilder = new PublishRequestBuilder<>();
        publishRequestBuilder.setGeneralParametersFromMap(attributesMap);
        validateInput(messages);
        PublishRequest publishRequest = publishRequestBuilder.generatePublishRequest(
            osduFileTopic,
            amazonSnsTopic,
            Arrays.asList(messages));
        snsClient.publish(publishRequest);
    }

    private void validateInput(Message[] messages) throws CoreException {
        validateMsg(messages);
    }

    private void validateMsg(Message[] messages) throws CoreException {
        if (messages == null || messages.length == 0) {
            throw new CoreException("Nothing in message to publish");
        }
    }
}
