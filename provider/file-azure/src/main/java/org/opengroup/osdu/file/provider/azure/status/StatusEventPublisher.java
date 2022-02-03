package org.opengroup.osdu.file.provider.azure.status;

import java.util.Arrays;
import java.util.Map;

import org.opengroup.osdu.azure.publisherFacade.MessagePublisher;
import org.opengroup.osdu.azure.publisherFacade.PublisherInfo;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.provider.azure.config.EventGridConfig;
import org.opengroup.osdu.file.provider.azure.config.PublisherConfig;
import org.opengroup.osdu.file.provider.azure.config.ServiceBusConfig;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusEventPublisher implements IEventPublisher {

    private static final String STATUS_CHANGED = "status-changed";
    private static final String EVENT_DATA_VERSION = "1.0";

    private final MessagePublisher messagePublisher;
    private final EventGridConfig eventGridConfig;
    private final ServiceBusConfig serviceBusConfig;
    private final PublisherConfig publisherConfig;
    private final DpsHeaders dpsHeaders;
    private final JaxRsDpsLog log;

    @Override
    public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
        validateInput(dpsHeaders, messages);
        publishMessage(dpsHeaders, messages);
        log.info("Status event generated successfully");
    }

    public void publishMessage(DpsHeaders dpsHeaders, Message[] messages) {
        final int BATCH_SIZE = Integer.parseInt(publisherConfig.getPubSubBatchSize());
        for (int i = 0; i < messages.length; i += BATCH_SIZE) {
            Message[] batch = Arrays.copyOfRange(messages, i, Math.min(messages.length, i + BATCH_SIZE));
            PublisherInfo publisherInfo = PublisherInfo.builder()
                .batch(batch)
                .eventGridTopicName(eventGridConfig.getStatusTopicName())
                .eventGridEventSubject(STATUS_CHANGED)
                .eventGridEventType(STATUS_CHANGED)
                .eventGridEventDataVersion(EVENT_DATA_VERSION)
                .serviceBusTopicName(serviceBusConfig.getServiceBusTopic())
                .build();
            messagePublisher.publishMessage(dpsHeaders, publisherInfo);
        }
    }

    private void validateInput(DpsHeaders dpsHeaders, Message[] messages) throws CoreException {
        validateMsg(messages);
        validateDpsHeaders(dpsHeaders);
    }

    private void validateMsg(Message[] messages) throws CoreException {
        if (messages == null || messages.length == 0) {
            throw new CoreException("Nothing in message to publish");
        }
    }

    private void validateDpsHeaders(DpsHeaders dpsHeaders) throws CoreException {
        if (dpsHeaders == null) {
          throw new CoreException("data-partition-id and correlation-id are required to publish status event");
        } else if (dpsHeaders.getPartitionId() == null) {
          throw new CoreException("data-partition-id is required to publish status event");
        } else if (dpsHeaders.getCorrelationId() == null) {
          throw new CoreException("correlation-id is required to publish status event");
        }
    }
}
