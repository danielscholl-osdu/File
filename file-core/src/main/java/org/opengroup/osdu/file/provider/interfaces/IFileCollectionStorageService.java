package org.opengroup.osdu.file.provider.interfaces;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedUrlParameters;

import java.util.List;

public interface IFileCollectionStorageService {

  /**
   * Generates Signed URL for File Upload Operations in DMS API Context.
   * @param datasetId Dataset ID
   * @param partitionID partition ID
   * @return info about object URI, upload signed URL etc.
   */
  default StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionID) {
    throw new NotImplementedException("Not implemented");
  }

  /**
   * Generates Signed URL for File Download Operations in DMS API Context.
   * @param fileRetrievalData List of Unsigned URLs for which Signed URL / Temporary credentials should be generated.
   * @return info about object URI, download signed URL etc.
   */
  default RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalData) {
    throw new NotImplementedException("Not implemented");
  }

  /**
   * Generates Signed URL for File Download Operations in DMS API Context.
   * @param fileRetrievalData: List of Unsigned URLs for which Signed URL / Temporary credentials should be generated.
   * @param signedUrlParameters: Signed url parameters, wrapping an optional expiryTime
   * @return info about object URI, download signed URL etc.
   */
  default RetrievalInstructionsResponse createRetrievalInstructions(List<FileRetrievalData> fileRetrievalData, SignedUrlParameters signedUrlParameters) {
    return createRetrievalInstructions(fileRetrievalData);
  }

  /**
   * Generates Signed URL for File Upload Operations in DMS API Context.
   * @param datasetId Dataset ID
   * @param partitionID partition ID
   * @param signedUrlParameters: Signed url parameters, wrapping an optional expiryTime
   * @return info about object URI, upload signed URL etc.
   */
  default  StorageInstructionsResponse createStorageInstructions(String datasetId, String partitionID,  SignedUrlParameters signedUrlParameters) {
    return createStorageInstructions(datasetId, partitionID) ;
  }
}
