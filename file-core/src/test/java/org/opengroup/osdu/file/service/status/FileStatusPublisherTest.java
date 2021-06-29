package org.opengroup.osdu.file.service.status;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.status.Status;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.core.common.status.StatusDetailsRequestBuilder;
import org.opengroup.osdu.file.model.storage.Record;

import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
class FileStatusPublisherTest {

    private static final String METADATA_STORE_STARTED = "Metadata store started";
    private static final String METADATA_STORE_COMPLETED_SUCCESSFULLY = "Metadata store completed successfully";
    private static final String STATUS_DETAILS_STR = "statusDetailsStr";
    private static final String METADATA_STORE_FAILED = "Metadata store failed";
    private static final int ERROR_CODE_INTERNAL_SERVER_ERROR = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private static final int ERROR_CODE_0 = 0;
    private static final String STAGE_DATASET_SYNC = "DATASET_SYNC";
    private static final String RECORD_ID = "recordId";
    private static final String DATA_PARTITION_ID = "data-partition-id";
    private static final String CORRELATION_ID = "correlation-id";

    @Mock
    private StatusDetailsRequestBuilder requestBuilder;

    @Mock
    private IEventPublisher statusPublisher;

    @InjectMocks
    private FileStatusPublisher fileStatusPublisher;

    private Map<String, String> attributesMap;

    @Before
    public void setup() {
        attributesMap = new HashMap<String, String>();
        attributesMap.put(DATA_PARTITION_ID, "partitionId");
        attributesMap.put(CORRELATION_ID, "correlationId");
    }

    @Test
    void testPublishStartStatus() throws JsonProcessingException {
        when(requestBuilder.createStatusDetailsMessage(METADATA_STORE_STARTED, null, Status.IN_PROGRESS,
                STAGE_DATASET_SYNC, ERROR_CODE_0)).thenReturn(STATUS_DETAILS_STR);
        when(requestBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishInProgressStatus();

        verify(requestBuilder, times(1)).createStatusDetailsMessage(METADATA_STORE_STARTED, null, Status.IN_PROGRESS,
                STAGE_DATASET_SYNC, ERROR_CODE_0);
        verify(requestBuilder, times(1)).createAttributesMap();
        verify(statusPublisher).publish(STATUS_DETAILS_STR, attributesMap);
    }

    @Test
    void testPublishFailureStatusWithNoRecordId() throws JsonProcessingException {
        Record record = new Record();

        when(requestBuilder.createStatusDetailsMessage(METADATA_STORE_FAILED, null, Status.FAILED, STAGE_DATASET_SYNC,
                ERROR_CODE_INTERNAL_SERVER_ERROR)).thenReturn(STATUS_DETAILS_STR);
        when(requestBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishFailureStatus(record, METADATA_STORE_FAILED);

        verify(requestBuilder, times(1)).createStatusDetailsMessage(METADATA_STORE_FAILED, null, Status.FAILED,
                STAGE_DATASET_SYNC, ERROR_CODE_INTERNAL_SERVER_ERROR);
        verify(requestBuilder, times(1)).createAttributesMap();
        verify(statusPublisher).publish(STATUS_DETAILS_STR, attributesMap);
    }

    @Test
    void testPublishFailureStatusWithRecordId() throws JsonProcessingException {
        Record record = new Record();
        record.setId(RECORD_ID);

        when(requestBuilder.createStatusDetailsMessage(METADATA_STORE_FAILED, record.getId(), Status.FAILED,
                STAGE_DATASET_SYNC, ERROR_CODE_INTERNAL_SERVER_ERROR)).thenReturn(STATUS_DETAILS_STR);
        when(requestBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishFailureStatus(record, METADATA_STORE_FAILED);

        verify(requestBuilder, times(1)).createStatusDetailsMessage(METADATA_STORE_FAILED, record.getId(),
                Status.FAILED, STAGE_DATASET_SYNC, ERROR_CODE_INTERNAL_SERVER_ERROR);
        verify(requestBuilder, times(1)).createAttributesMap();
        verify(statusPublisher).publish(STATUS_DETAILS_STR, attributesMap);
    }

    @Test
    void testPublishSuccessStatus() throws JsonProcessingException {
        Record record = new Record();
        record.setId(RECORD_ID);
        when(requestBuilder.createStatusDetailsMessage(METADATA_STORE_COMPLETED_SUCCESSFULLY, RECORD_ID, Status.SUCCESS,
                STAGE_DATASET_SYNC, ERROR_CODE_0)).thenReturn(STATUS_DETAILS_STR);
        when(requestBuilder.createAttributesMap()).thenReturn(attributesMap);

        fileStatusPublisher.publishSuccessStatus(record);

        verify(requestBuilder, times(1)).createStatusDetailsMessage(METADATA_STORE_COMPLETED_SUCCESSFULLY, RECORD_ID,
                Status.SUCCESS, STAGE_DATASET_SYNC, ERROR_CODE_0);
        verify(requestBuilder, times(1)).createAttributesMap();
        verify(statusPublisher).publish(STATUS_DETAILS_STR, attributesMap);
    }

}
