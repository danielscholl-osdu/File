package org.opengroup.osdu.file.service.status;

import java.util.Map;

import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.Status;
import org.opengroup.osdu.core.common.model.status.StatusDetails;
import org.opengroup.osdu.core.common.model.status.StatusDetails.Properties;
import org.opengroup.osdu.core.common.status.AttributesBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileStatusPublisher {

    private static final String KIND = "status";
    private static final String FAILED_TO_PUBLISH_STATUS = "Failed to publish status ";
    private static final String DATASET_SYNC = "DATASET_SYNC";

    private final IEventPublisher statusEventPublisher;
    private final AttributesBuilder attributesBuilder;
    private final DpsHeaders dpsHeaders;
    private final JaxRsDpsLog log;

    public void publishInProgressStatus() {
        Map<String, String> attributesMap = attributesBuilder.createAttributesMap();
        StatusDetails[] statusDetailsArr = createStatusDetailsArr("Metadata store started", null, null,
                Status.IN_PROGRESS, DATASET_SYNC, 0);

        publish(statusDetailsArr, attributesMap);
    }

    public void publishSuccessStatus(String recordId, String recordIdVersion) {
        Map<String, String> attributesMap = attributesBuilder.createAttributesMap();
        StatusDetails[] statusDetailsArr = createStatusDetailsArr("Metadata store completed successfully", recordId,
                recordIdVersion, Status.SUCCESS, DATASET_SYNC, 0);

        publish(statusDetailsArr, attributesMap);
    }

    public void publishFailureStatus(String message, int errorCode) {
        Map<String, String> attributesMap = attributesBuilder.createAttributesMap();
        StatusDetails[] statusDetailsArr = createStatusDetailsArr(message, null, null, Status.FAILED, DATASET_SYNC,
                errorCode);

        publish(statusDetailsArr, attributesMap);
    }

    public void publishFailureStatus(HttpResponse httpResponse) {
        String errorMessage = null;
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject =  gson.fromJson(httpResponse.getBody(), JsonObject.class);

        errorMessage = jsonObject != null ? jsonObject.get("message").getAsString() : "Http Error";

        publishFailureStatus(errorMessage, httpResponse.getResponseCode());
    }

    private void publish(StatusDetails[] statusDetailsArr, Map<String, String> attributesMap) {
        try {
            statusEventPublisher.publish(statusDetailsArr, attributesMap);
        } catch (CoreException e) {
            log.warning(FAILED_TO_PUBLISH_STATUS + e.getMessage(), e);
        }
    }

    private StatusDetails[] createStatusDetailsArr(String msg, String recordId, String recordIdVersion, Status status,
            String stage, int errorCode) {
        StatusDetails[] statusDetailsArr = new StatusDetails[1];
        statusDetailsArr[0] = createStatusDetails(msg, recordId, recordIdVersion, status, stage, errorCode);

        return statusDetailsArr;
    }

    private StatusDetails createStatusDetails(String msg, String recordId, String recordIdVersion, Status status,
            String stage, int errorCode) {
        Properties properties = createProperties(msg, recordId, recordIdVersion, status, stage, errorCode);
        StatusDetails statusDetails = new StatusDetails();
        statusDetails.setKind(KIND);
        statusDetails.setProperties(properties);
        return statusDetails;
    }

    private Properties createProperties(String msg, String recordId, String recordIdVersion, Status status,
            String stage, int errorCode) {
        StatusDetails statusDetails = new StatusDetails();
        Properties properties = statusDetails.new Properties();
        properties.setCorrelationId(dpsHeaders.getCorrelationId());
        properties.setErrorCode(errorCode);
        properties.setMessage(msg);
        properties.setRecordId(recordId);
        properties.setRecordIdVersion(recordIdVersion);
        properties.setStage(stage);
        properties.setStatus(status);
        properties.setTimestamp(System.currentTimeMillis());
        properties.setUserEmail(dpsHeaders.getUserEmail());
        return properties;
    }
}