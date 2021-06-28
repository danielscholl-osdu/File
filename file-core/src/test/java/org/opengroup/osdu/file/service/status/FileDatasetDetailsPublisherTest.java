package org.opengroup.osdu.file.service.status;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.DatasetType;
import org.opengroup.osdu.core.common.status.DatasetDetailsRequestBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.model.storage.Record;

import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
class FileDatasetDetailsPublisherTest {

    private static final String DATASET_DETAILS_STR = "datasetDetailsStr";

    @Mock
    private DatasetDetailsRequestBuilder requestBuilder;

    @Mock
    private IEventPublisher datasetDetailsEventPublisher;

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private FileDatasetDetailsPublisher fileDatasetDetailsPublisher;

    @Test
    void testDatasetDetailsPublishSuccess() throws JsonProcessingException {

        Record record = createRecord();
        Map<String, String> attributesMap = createAttributesMap();

        when(requestBuilder.createDatasetDetailsMessage(record.getId(), DatasetType.FILE, record.getVersion() + "", 1))
                .thenReturn(DATASET_DETAILS_STR);
        when(requestBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileDatasetDetailsPublisher.publishDatasetDetails(record);

        verify(datasetDetailsEventPublisher).publish(DATASET_DETAILS_STR, attributesMap);
    }

    private Record createRecord() {
        Record record = new Record();
        record.setId("id");
        record.setVersion(1234L);
        return record;
    }

    private Map<String, String> createAttributesMap() {
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put(DpsHeaders.DATA_PARTITION_ID, "data-partition-id");
        attributesMap.put(DpsHeaders.CORRELATION_ID, "correlation-id");
        return attributesMap;
    }

}
