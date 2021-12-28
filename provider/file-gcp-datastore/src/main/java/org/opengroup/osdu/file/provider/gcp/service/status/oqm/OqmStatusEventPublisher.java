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

package org.opengroup.osdu.file.provider.gcp.service.status.oqm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.core.gcp.oqm.driver.OqmDriver;
import org.opengroup.osdu.core.gcp.oqm.model.OqmDestination;
import org.opengroup.osdu.core.gcp.oqm.model.OqmMessage;
import org.opengroup.osdu.core.gcp.oqm.model.OqmTopic;
import org.opengroup.osdu.file.provider.gcp.config.properties.GcpConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gcp.status.changed.messaging.enabled", havingValue = "true", matchIfMissing = false)
public class OqmStatusEventPublisher implements IEventPublisher {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final GcpConfigurationProperties configurationProperties;
  private final OqmDriver oqmDriver;
  private final TenantInfo tenantInfo;
  private OqmTopic oqmTopic;

  @PostConstruct
  void postConstruct() {
    oqmTopic = OqmTopic.builder().name(configurationProperties.getStatusChangedTopic()).build();
  }

  @Override
  public void publish(Message[] messages,
      Map<String, String> attributesMap) {
    validateInput(messages, attributesMap);
    Map<String, Object> messageMap = createMessageMap(messages, attributesMap);

    String messageJsonValue = null;
    try {
      messageJsonValue = objectMapper.writeValueAsString(messageMap);
    } catch (JsonProcessingException e) {
      log.warn("Unable to process messages: {}, error: {}", messages, e.getMessage(), e);
    }
    OqmDestination oqmDestination =
        OqmDestination.builder().partitionId(tenantInfo.getDataPartitionId()).build();
    OqmMessage oqmMessage =
        OqmMessage.builder().data(messageJsonValue).attributes(attributesMap).build();
    oqmDriver.publish(oqmMessage, oqmTopic, oqmDestination);
    log.debug("Publishing to topic: {} Message: {} ",
        configurationProperties.getStatusChangedTopic(),
        messageJsonValue);
  }

  private Map<String, Object> createMessageMap(Message[] messages,
      Map<String, String> attributesMap) {
    String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);
    String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
    Map<String, Object> message = new HashMap<>();
    message.put("data", messages);
    message.put(DpsHeaders.DATA_PARTITION_ID, dataPartitionId);
    message.put(DpsHeaders.CORRELATION_ID, correlationId);
    message.put(DpsHeaders.ACCOUNT_ID, this.tenantInfo.getName());
    return message;
  }

  private void validateInput(Message[] messages, Map<String, String> attributesMap) {
    validateMsg(messages);
    validateAttributesMap(attributesMap);
  }

  private void validateMsg(Message[] messages) {
    if (Objects.isNull(messages) || messages.length == 0) {
      log.warn("Nothing in message to publish");
    }
  }

  private void validateAttributesMap(Map<String, String> attributesMap) {
    if (Objects.isNull(attributesMap) || attributesMap.isEmpty()) {
      throw new IllegalArgumentException(
          "data-partition-id and correlation-id are required to publish status event");
    } else if (attributesMap.get(DpsHeaders.DATA_PARTITION_ID) == null) {
      throw new IllegalArgumentException("data-partition-id is required to publish status event");
    } else if (attributesMap.get(DpsHeaders.CORRELATION_ID) == null) {
      throw new IllegalArgumentException("correlation-id is required to publish status event");
    }
  }
}
