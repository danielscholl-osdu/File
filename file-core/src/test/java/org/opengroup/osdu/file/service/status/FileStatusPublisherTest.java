package org.opengroup.osdu.file.service.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.AttributesBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.model.storage.Record;

import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
class FileStatusPublisherTest {

    private static final String ERROR_MESSAGE = "error-message";
    private static final String RECORD_ID_VERSION = "recordIdVersion";
    private static final String RECORD_ID = "recordId";
    private static final String DATA_PARTITION_ID = "data-partition-id";
    private static final String CORRELATION_ID = "correlation-id";

    @Mock
    private AttributesBuilder attributesBuilder;

    @Mock
    private IEventPublisher statusPublisher;
    
    @Mock
    private DpsHeaders dpsHeaders;

    @InjectMocks
    private FileStatusPublisher fileStatusPublisher;

    private Map<String, String> attributesMap;

    @BeforeEach
    public void setup() {
        attributesMap = new HashMap<String, String>();
        attributesMap.put(DATA_PARTITION_ID, "partitionId");
        attributesMap.put(CORRELATION_ID, "correlationId");
    }

    @Test
    void testPublishStartStatus() throws JsonProcessingException {
        when(attributesBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishInProgressStatus();

        verify(attributesBuilder, times(1)).createAttributesMap();
        verify(statusPublisher, times(1)).publish(any(Message[].class), any(Map.class));
    }

    @Test
    void testPublishFailureStatus() throws JsonProcessingException {
        when(attributesBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishFailureStatus(ERROR_MESSAGE, 400);

        verify(attributesBuilder, times(1)).createAttributesMap();
        verify(statusPublisher, times(1)).publish(any(Message[].class), any(Map.class));
    }

    @Test
    void testPublishFailureStatusWithHttpResponse() throws JsonProcessingException {
        Record record = new Record();
        record.setId(RECORD_ID);

        when(attributesBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishFailureStatus(new HttpResponse());

        verify(attributesBuilder, times(1)).createAttributesMap();
        verify(statusPublisher, times(1)).publish(any(Message[].class), any(Map.class));
    }

    @Test
    void testPublishSuccessStatus() throws JsonProcessingException {
        Record record = new Record();
        record.setId(RECORD_ID);
        when(attributesBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishSuccessStatus(RECORD_ID, RECORD_ID_VERSION);

        verify(attributesBuilder, times(1)).createAttributesMap();
        verify(statusPublisher, times(1)).publish(any(Message[].class), any(Map.class));
    }

}
