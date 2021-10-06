package org.opengroup.osdu.file.provider.reference.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(
    name = {"rabbitmq.enabled"},
    havingValue = "false",
    matchIfMissing = false
)
public class StatusEventPublisherStub implements IEventPublisher {

  @Override
  public void publish(Message[] messages, Map<String, String> attributes) {
    String correlationId = attributes.get("correlation-id");
    String dataPartitionId = attributes.get("data-partition-id");
    log.debug("Status changed messaging disabled, writing message to log.");
    log.debug("Correlation-id: {}. Data-partition-id: {}. Status messages: {}" + correlationId,
        dataPartitionId, messages);
  }
}