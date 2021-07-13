package org.opengroup.osdu.file.service.status;

import java.util.Map;

import org.opengroup.osdu.core.common.exception.CoreException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.status.DatasetDetails;
import org.opengroup.osdu.core.common.model.status.DatasetDetails.Properties;
import org.opengroup.osdu.core.common.model.status.DatasetType;
import org.opengroup.osdu.core.common.status.AttributesBuilder;
import org.opengroup.osdu.core.common.status.IEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileDatasetDetailsPublisher {
	
	private static final String KIND = "datasetDetails";
    private static final DatasetType DATASET_TYPE = DatasetType.FILE;
    private static final String FAILED_TO_PUBLISH_DATASET_DETAILS = "Failed to publish dataset details";

    private final IEventPublisher datasetDetailsEventPublisher;
    private final AttributesBuilder attributesBuilder;
    private final DpsHeaders dpsHeaders;
    private final JaxRsDpsLog log;

    public void publishDatasetDetails(String datasetId, String datasetVersionId) {
        Map<String, String> attributesMap = attributesBuilder.createAttributesMap();
        int recordCount = 1;
        DatasetDetails[] datasetDetailsArr = createDatasetDetailsArr(datasetId, datasetVersionId, recordCount);
        try {
            datasetDetailsEventPublisher.publish(datasetDetailsArr, attributesMap);
        } catch (CoreException e) {
            log.warning(FAILED_TO_PUBLISH_DATASET_DETAILS + e.getMessage());
        }
    }

    private DatasetDetails[] createDatasetDetailsArr(String datasetId, String datasetVersionId, int recordCount) {
        DatasetDetails datasetDetails = createDatasetDetails(datasetId, DATASET_TYPE, datasetVersionId, recordCount);
        DatasetDetails[] datasetDetailsArr = new DatasetDetails[1];
        datasetDetailsArr[0] = datasetDetails;
        return datasetDetailsArr;
    }

    private DatasetDetails createDatasetDetails(String datasetId, DatasetType datasetType, String datasetVersionId,
            int recordCount) {
        Properties properties = createProperties(datasetId, datasetType, datasetVersionId, recordCount);
        DatasetDetails datasetDetails = new DatasetDetails();
        datasetDetails.setKind(KIND);
        datasetDetails.setProperties(properties);
        return datasetDetails;
    }

    private Properties createProperties(String datasetId, DatasetType datasetType, String datasetVersionId,
            int recordCount) {
        DatasetDetails datasetDetails = new DatasetDetails();
        Properties properties = datasetDetails.new Properties();
        properties.setCorrelationId(dpsHeaders.getCorrelationId());
        properties.setDatasetId(datasetId);
        properties.setDatasetType(datasetType);
        properties.setDatasetVersionId(datasetVersionId);
        properties.setRecordCount(recordCount);
        properties.setTimestamp(System.currentTimeMillis());
        return properties;
    }

}
