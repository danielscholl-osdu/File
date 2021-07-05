/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.file.model.SignedUrl;

public interface IStorageService {

  /**
   * Creates the empty object blob in storage.
   * Bucket name is determined by tenant using {@code partitionID}.
   * Object name is concat of a filepath and a fileID. Filepath is determined by user.
   *
   * @param fileID file ID
   * @param authorizationToken authorization token
   * @param partitionID partition ID
   * @return info about object URI, signed URL and when and who created blob.
   */
  SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID);

  // stub implementation

  /**
   * Generates Signed URL for File Upload Operations in DMS API Context.
   * @param datasetId Dataset ID
   * @param authorizationToken Authorization token
   * @param partitionID partition ID
   * @return info about object URI, signed URL etc.
   */
  default StorageInstructionsResponse createStorageInstructions(String datasetId, String authorizationToken, String partitionID) {
    return null;
  }

  //stub Implementation
  /**
   * Gets a signed url from an unsigned url
   *
   * @param unsignedUrl
   * @param authorizationToken
   * @return
   */
  default SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken) {
    return null;
  }
}
