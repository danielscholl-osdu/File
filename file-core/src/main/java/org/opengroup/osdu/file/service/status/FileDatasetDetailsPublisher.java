package org.opengroup.osdu.file.service.status;

import java.util.Map;

import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.status.DatasetType;
import org.opengroup.osdu.core.common.status.DatasetDetailsRequestBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileDatasetDetailsPublisher {
    private final DatasetDetailsRequestBuilder requestBuilder;
    private final IEventPublisher datasetDetailsEventPublisher;
    private final JaxRsDpsLog log;

    private static final DatasetType DATASET_TYPE = DatasetType.FILE;
    private static final String FAILED_TO_PUBLISH_DATASET_DETAILS = "Failed to publish dataset details";

    public void publishDatasetDetails(Record record) {
        Map<String, String> attributesMap = requestBuilder.createAttributesMap();
        String datasetId = record.getId();
        String datasetVersionId = record.getVersion() + "";
        int recordCount = 1;
        String datasetDetailsMessage = null;

        try {
            datasetDetailsMessage = requestBuilder.createDatasetDetailsMessage(datasetId, DATASET_TYPE,
                    datasetVersionId, recordCount);
            datasetDetailsEventPublisher.publish(datasetDetailsMessage, attributesMap);
        } catch (CoreException | JsonProcessingException e) {
            log.warning(FAILED_TO_PUBLISH_DATASET_DETAILS + e.getMessage());
        }
    }

}
