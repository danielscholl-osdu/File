package org.opengroup.osdu.file.provider.interfaces;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.springframework.context.annotation.Configuration;

import java.util.List;

public interface IFileCollectionStorageService {

  /**
   * Gets a signed url from an unsigned url
   *
   * @param unsignedUrl
   * @param authorizationToken
   * @param signedUrlParameters
   * @return
   */

  default SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken, SignedUrlParameters signedUrlParameters) {
    throw new NotImplementedException("Not implemented");
  }

  /**
   * Creates the empty directory in storage.
   * and get signed url for uploading the files.
   *
   * @param fileID file ID
   * @param authorizationToken authorization token
   * @param partitionID partition ID
   * @return info about object URI, signed URL and when and who created blob.
   */
  default SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
    throw new NotImplementedException("Not implemented");
  }

  // stub implementation
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
}
