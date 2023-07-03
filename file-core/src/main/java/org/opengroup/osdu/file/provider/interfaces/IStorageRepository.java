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

import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.model.SignedObject;

import java.util.Map;

public interface IStorageRepository {

  /**
   * Creates the empty object blob in bucket by filepath.
   *
   * @param bucketName bucket name
   * @param filepath file path
   * @return info created blob and signed URL
   */
  SignedObject createSignedObject(String bucketName, String filepath);

  /**
   * Get signed object for blob in bucket by filepath.
   *
   * @param bucketName bucket name
   * @param filepath file path
   * @return info blob and signed URL
   */
  default SignedObject getSignedObject(String bucketName, String filepath){
    return null;
  }

  /**
   * Get signed object for blob in bucket by filepath based on custom urlParameters like expiryTime.
   *
   * @param bucketName bucket name
   * @param filepath file path
   * @param signedUrlParameters
   * @return info blob and signed URL
   */
  default SignedObject getSignedObjectBasedOnParams(String bucketName, String filepath,
      SignedUrlParameters signedUrlParameters) {
    return getSignedObject(bucketName,filepath);
  }

  /**
   * Revokes the Signed URLs based on the request.
   *
   * @param revokeURLRequest Map of properties required to revoke urls eg: storage account name
   * @return true if urls are revoked successfully, otherwise false
   */
  default Boolean revokeUserDelegationKeys(Map<String, String> revokeURLRequest){
    return false;
  }

}
