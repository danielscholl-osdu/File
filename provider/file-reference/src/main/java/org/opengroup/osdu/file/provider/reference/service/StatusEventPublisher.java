/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.reference.service;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.provider.reference.config.RabbitMqConfigProperties;
import org.opengroup.osdu.file.provider.reference.factory.RabbitMqFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = {"rabbitmq.enabled"},
    havingValue = "true",
    matchIfMissing = false
)
public class StatusEventPublisher implements IEventPublisher {

  private static final String DATA = "data";
  private static final String DATA_PARTITION_ID = "data-partition-id";
  private static final String CORRELATION_ID = "correlation-id";

  private final RabbitMqFactory rabbitMqFactory;
  private final RabbitMqConfigProperties properties;

  @Override
  public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
    this.validateInput(messages, attributesMap);
    Map<String, Object> messageMap = this.createMessageMap(messages, attributesMap);
    String payload = new Gson().toJson(messageMap);
    Channel client = rabbitMqFactory.getClient();
    String topicName = properties.getTopicName();
    try {
      client.basicPublish("", topicName, null, payload.getBytes());
      log.info(String.format("[x] Sent '%s' to queue [%s]", payload, topicName));
    } catch (IOException e) {
      log.error(String.format("Unable to publish payload to [%s]", topicName));
      log.error(e.getMessage(), e);
    }
  }

  private void validateInput(Message[] messages, Map<String, String> attributesMap) {
    this.validateMsg(messages);
    this.validateAttributesMap(attributesMap);
  }

  private void validateMsg(Message[] messages) {
    if (Objects.isNull(messages) || messages.length == 0) {
      log.warn("Nothing in message to publish");
    }
  }

  private void validateAttributesMap(Map<String, String> attributesMap) {
    if (!Objects.isNull(attributesMap) && !attributesMap.isEmpty()) {
      if (attributesMap.get(DATA_PARTITION_ID) == null) {
        throw new IllegalArgumentException("data-partition-id is required to publish status event");
      } else if (attributesMap.get(CORRELATION_ID) == null) {
        throw new IllegalArgumentException("correlation-id is required to publish status event");
      }
    } else {
      throw new IllegalArgumentException(
          "data-partition-id and correlation-id are required to publish status event");
    }
  }

  private Map<String, Object> createMessageMap(
      Message[] messages, Map<String, String> attributesMap) {
    String dataPartitionId = attributesMap.get(DATA_PARTITION_ID);
    String correlationId = attributesMap.get(CORRELATION_ID);
    Map<String, Object> message = new HashMap();
    message.put(DATA, messages);
    message.put(DATA_PARTITION_ID, dataPartitionId);
    message.put(CORRELATION_ID, correlationId);
    return message;
  }
}
