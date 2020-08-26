/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
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

package org.opengroup.osdu.file.gcp.util;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.opengroup.osdu.file.util.CloudStorageUtil;

public class StorageUtilGCP extends CloudStorageUtil {

  private Storage storage;

  public StorageUtilGCP() {
    storage = StorageOptions.newBuilder()
        .setCredentials(StorageServiceAccountCredentialsProvider.getCredentials())
        .setProjectId(GcpConfig.getProjectID())
        .build()
        .getService();
  }

  @Override
  public void deleteCloudFile(String bucketName, String fileName) {
    if (storage.delete(BlobId.of(bucketName, fileName))) {
      System.out
          .println(String.format("Test file %s deleted from bucket %s", fileName, bucketName));
    } else {
      System.out.println(
          String.format("Test file %s was not deleted from bucket %s", fileName, bucketName));
    }

  }
}
