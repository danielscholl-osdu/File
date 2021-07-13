package org.opengroup.osdu.file.provider.azure.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.provider.azure.config.EventGridConfig;
import org.springframework.stereotype.Service;

import com.microsoft.azure.eventgrid.models.EventGridEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusEventPublisher implements IEventPublisher {

    private static final String STATUS_CHANGED = "status-changed";
    private static final String EVENT_DATA_VERSION = "1.0";

    private final EventGridTopicStore eventGridTopicStore;
    private final EventGridConfig eventGridConfig;
    private final JaxRsDpsLog log;

    @Override
    public void publish(Message[] messages, Map<String, String> attributesMap) throws CoreException {
        validateEventGrid();
        validateInput(messages, attributesMap);

        HashMap<String, Object> message = createMessageMap(messages, attributesMap);
        List<EventGridEvent> eventsList = createEventGridEventList(message);

        eventGridTopicStore.publishToEventGridTopic(attributesMap.get(DpsHeaders.DATA_PARTITION_ID),
                eventGridConfig.getCustomTopicName(), eventsList);
        log.info("Status event generated successfully");
        
    }

    private List<EventGridEvent> createEventGridEventList(HashMap<String, Object> message) {
        String messageId = UUID.randomUUID().toString();
        List<EventGridEvent> eventsList = new ArrayList<>();
        EventGridEvent eventGridEvent = new EventGridEvent(messageId, STATUS_CHANGED, message, STATUS_CHANGED,
                DateTime.now(), EVENT_DATA_VERSION);
        eventsList.add(eventGridEvent);
        return eventsList;
    }

    private HashMap<String, Object> createMessageMap(Message[] messages, Map<String, String> attributesMap) {
        String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);
        String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
        HashMap<String, Object> message = new HashMap<>();
        message.put("data", messages);
        message.put(DpsHeaders.DATA_PARTITION_ID, dataPartitionId);
        message.put(DpsHeaders.CORRELATION_ID, correlationId);
        return message;
    }

    private void validateEventGrid() throws CoreException {
        if (!eventGridConfig.isEventGridEnabled()) {
            throw new CoreException("Event grid is not enabled");
        }
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
