package org.opengroup.osdu.file.service.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Message;
import org.opengroup.osdu.core.common.status.AttributesBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
class FileDatasetDetailsPublisherTest {


    @Mock
    private AttributesBuilder attributesBuilder;

    @Mock
    private IEventPublisher datasetDetailsEventPublisher;
    
    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private FileDatasetDetailsPublisher fileDatasetDetailsPublisher;

    @Test
    void testDatasetDetailsPublishSuccess() throws JsonProcessingException {

        Map<String, String> attributesMap = createAttributesMap();

        when(attributesBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileDatasetDetailsPublisher.publishDatasetDetails("record-id", "record-id-version");

        verify(datasetDetailsEventPublisher).publish(any(Message[].class), any(Map.class));
    }

    private Map<String, String> createAttributesMap() {
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put(DpsHeaders.DATA_PARTITION_ID, "data-partition-id");
        attributesMap.put(DpsHeaders.CORRELATION_ID, "correlation-id");
        return attributesMap;
    }

}
