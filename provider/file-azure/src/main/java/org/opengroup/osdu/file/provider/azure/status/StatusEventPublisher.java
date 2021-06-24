package org.opengroup.osdu.file.provider.azure.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.StatusPublishException;
import org.opengroup.osdu.file.provider.azure.config.EventGridConfig;
import org.opengroup.osdu.file.provider.interfaces.IStatusEventPublisher;
import org.springframework.stereotype.Service;

import com.microsoft.azure.eventgrid.models.EventGridEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusEventPublisher implements IStatusEventPublisher {

    private static final String STATUS_CHANGED = "status-changed";
    private static final String EVENT_DATA_VERSION = "1.0";

    private final EventGridTopicStore eventGridTopicStore;
    private final EventGridConfig eventGridConfig;
    private final JaxRsDpsLog log;

    @Override
    public void publish(String msg, Map<String, String> attributesMap) throws StatusPublishException {
        validateEventGrid();
        validateInput(msg, attributesMap);

        HashMap<String, Object> message = createMessageMap(msg, attributesMap);
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
        eventsList.add(eventGridEvent); // TODO check usage of events list
        log.info("Status event created: " + messageId);
        return eventsList;
    }

    private HashMap<String, Object> createMessageMap(String msg, Map<String, String> attributesMap) {
        String dataPartitionId = attributesMap.get(DpsHeaders.DATA_PARTITION_ID);
        String correlationId = attributesMap.get(DpsHeaders.CORRELATION_ID);
        HashMap<String, Object> message = new HashMap<>();
        message.put("data", msg);
        message.put(DpsHeaders.DATA_PARTITION_ID, dataPartitionId);
        message.put(DpsHeaders.CORRELATION_ID, correlationId);
        return message;
    }

    private void validateEventGrid() throws StatusPublishException {
        if (!eventGridConfig.isEventGridEnabled()) {
            throw new StatusPublishException("Event grid is not enabled");
        }
    }

    private void validateInput(String msg, Map<String, String> attributesMap) throws StatusPublishException {
        validateMsg(msg);
        validateAttributesMap(attributesMap);
    }

    private void validateMsg(String msg) throws StatusPublishException {
        if (msg == null || msg.isEmpty()) {
            throw new StatusPublishException("Nothing in message to publish");
        }
    }

    private void validateAttributesMap(Map<String, String> attributesMap) throws StatusPublishException {
        if (attributesMap.isEmpty()) {
            throw new StatusPublishException("data-partition-id and correlation-id are required to publish status event");
        } else if (attributesMap.get(DpsHeaders.DATA_PARTITION_ID) == null) {
            throw new StatusPublishException("data-partition-id is required to publish status event");
        } else if (attributesMap.get(DpsHeaders.CORRELATION_ID) == null) {
            throw new StatusPublishException("correlation-id is required to publish status event");
        }
    }

}
