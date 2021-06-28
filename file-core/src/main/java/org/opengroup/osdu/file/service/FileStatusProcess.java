package org.opengroup.osdu.file.service;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.status.Status;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.core.common.status.StatusDetailsRequestBuilder;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileStatusProcess {

    private static final String FAILED_TO_PUBLISH_STATUS = "Failed to publish status ";
    private static final String DATASET_SYNC = "DATASET_SYNC";

    private final StatusDetailsRequestBuilder requestBuilder;
    private final IEventPublisher statusEventPublisher;
    private final JaxRsDpsLog log;

    public void publishStartStatus() {
        Map<String, String> attributesMap = requestBuilder.createAttributesMap();

        try {
            String statusDetailsMessage = requestBuilder.createStatusDetailsMessage("Metadata store started", null,
                    Status.IN_PROGRESS, DATASET_SYNC, 0);
            statusEventPublisher.publish(statusDetailsMessage, attributesMap);
        } catch (CoreException | JsonProcessingException e) {
            log.warning(FAILED_TO_PUBLISH_STATUS + e.getMessage());
        }
    }

    public void publishSuccessStatus(String recordId) {
        Map<String, String> attributesMap = requestBuilder.createAttributesMap();

        try {
            String statusDetailsMessage = requestBuilder.createStatusDetailsMessage(
                    "Metadata store completed successfully", recordId, Status.SUCCESS, DATASET_SYNC, 0);
            statusEventPublisher.publish(statusDetailsMessage, attributesMap);
        } catch (CoreException | JsonProcessingException e) {
            log.warning(FAILED_TO_PUBLISH_STATUS + e.getMessage());
        }
    }

    public void publishFailureStatus(Record record) {
        Map<String, String> attributesMap = requestBuilder.createAttributesMap();
        String statusDetailsMessage = null;

        try {
            if (record != null) {
                statusDetailsMessage = requestBuilder.createStatusDetailsMessage("Metadata store failed",
                        record.getId(), Status.FAILED, DATASET_SYNC, HttpStatus.SC_INTERNAL_SERVER_ERROR);

            } else {
                statusDetailsMessage = requestBuilder.createStatusDetailsMessage("Metadata store failed", null,
                        Status.FAILED, DATASET_SYNC, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
            statusEventPublisher.publish(statusDetailsMessage, attributesMap);
        } catch (CoreException | JsonProcessingException e) {
            log.warning(FAILED_TO_PUBLISH_STATUS + e.getMessage());
        }
    }
}