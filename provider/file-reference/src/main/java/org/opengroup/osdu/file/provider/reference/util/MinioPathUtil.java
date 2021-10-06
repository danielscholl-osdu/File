/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.reference.util;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MinioPathUtil {

  public String getCompleteFilePath(String destinationBucket, String filePath) {
    return "https://" + destinationBucket + filePath;
  }

  public String getBucketName(String filePath) {

    String[] filePathChunks = filePath.split(FileMetadataConstant.FORWARD_SLASH);
    String sourceBucket = "";

    if (filePathChunks.length > 1) {
      sourceBucket = filePathChunks[3];
    }

    return sourceBucket;
  }

  public String getFolderName(String filePath) {
    return filePath.split(FileMetadataConstant.FORWARD_SLASH)[0]
        + FileMetadataConstant.FORWARD_SLASH;
  }

  public String getDirectoryPath(String filePath) {
    String result = "";
    String bucketName = getBucketName(filePath);

    int initialIndex = filePath.indexOf(bucketName) + bucketName.length() + 1;
    int lastIndex = filePath.length();
    if ((lastIndex > 0) && (lastIndex > initialIndex)) {
      result = filePath.substring(initialIndex, lastIndex);
    }
    return result;
  }
}
