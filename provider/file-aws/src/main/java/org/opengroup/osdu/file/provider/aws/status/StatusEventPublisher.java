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

package org.opengroup.osdu.file.provider.aws.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opengroup.osdu.core.aws.ssm.ParameterStorePropertySource;
import org.opengroup.osdu.core.aws.ssm.SSMConfig;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.amazonaws.services.sns.AmazonSNS;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.springframework.beans.factory.annotation.Value;
import com.amazonaws.services.sns.model.PublishRequest;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;

import javax.annotation.PostConstruct;

import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;


@Service
@RequiredArgsConstructor
public class StatusEventPublisher implements IEventPublisher {

	private static final String STATUS_CHANGED = "status-changed";
	private static final String EVENT_DATA_VERSION = "1.0";

	@Value("${aws.sns.region}")
	private String amazonSNSRegion;

	@Value("${aws.sns.topic.arn}")
	private String snsTopicArn;

	private AmazonSNS snsClient;
	private String amazonSNSTopic;

	private final JaxRsDpsLog log;

	@PostConstruct
	public void init() throws K8sParameterNotFoundException {
		AmazonSNSConfig snsConfig = new AmazonSNSConfig(amazonSNSRegion);
		snsClient = snsConfig.AmazonSNS();
		SSMConfig ssmConfig = new SSMConfig();
		amazonSNSTopic = ssmConfig.amazonSSM().getProperty(snsTopicArn).toString();
	}

	@Override
	public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
		validateInput(messages, attributesMap);
		PublishRequest publishRequest = new PublishRequestBuilder().generatePublishRequest(
			"data",
			Arrays.asList(messages),
			createMessageMap(messages, attributesMap),
			amazonSNSTopic);
		snsClient.publish(publishRequest);
	}

	private HashMap<String, MessageAttributeValue> createMessageMap(Message[] messages, Map<String, String> attributesMap) {
		HashMap<String, MessageAttributeValue> messageAttributes = new HashMap<>();
		messageAttributes.put(DpsHeaders.DATA_PARTITION_ID, new MessageAttributeValue()
			.withDataType("String")
			.withStringValue(attributesMap.get(DpsHeaders.DATA_PARTITION_ID)));
		messageAttributes.put(DpsHeaders.CORRELATION_ID, new MessageAttributeValue()
			.withDataType("String")
			.withStringValue(attributesMap.get(DpsHeaders.CORRELATION_ID)));
		return messageAttributes;
	}

	private void validateInput(Message[] messages, Map<String, String> attributesMap) throws CoreException {
		validateMsg(messages);
		validateAttributesMap(attributesMap);
	}

	private void validateMsg(Message[] messages) throws CoreException {
		if (messages == null || messages.length == 0) {
			throw new CoreException("Nothing in message to publish");
		}
	}

	private void validateAttributesMap(Map<String, String> attributesMap) throws CoreException {
		if (attributesMap == null || attributesMap.isEmpty()) {
			throw new CoreException("data-partition-id and correlation-id are required to publish status event");
		} else if (attributesMap.get(DpsHeaders.DATA_PARTITION_ID) == null) {
			throw new CoreException("data-partition-id is required to publish status event");
		} else if (attributesMap.get(DpsHeaders.CORRELATION_ID) == null) {
			throw new CoreException("correlation-id is required to publish status event");
		}
	}

}