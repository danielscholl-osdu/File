// Copyright © 2021 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.aws.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntTestS3Location {

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public String bucket;

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public String key;

  @Getter
  @Setter(AccessLevel.PRIVATE)
  public boolean isValid = false;

  private static final String UNSIGNED_URL_PREFIX = "s3://";
  private static final String SIGNED_URL_PREFIX = "https://";
  private static final String SIGNED_URL_DOMAIN = ".s3.amazonaws.com";

  public IntTestS3Location(String uri) {
    if (uri != null && uri.startsWith(UNSIGNED_URL_PREFIX)) {
      String[] bucketAndKey = uri.substring(UNSIGNED_URL_PREFIX.length()).split("/", 2);
      if (bucketAndKey.length == 2) {
        bucket = bucketAndKey[0];
        key = bucketAndKey[1];
        isValid = true;
      }
    }
    else if (uri != null && uri.startsWith(SIGNED_URL_PREFIX)) {
      String[] bucketAndKey = uri.substring(SIGNED_URL_PREFIX.length()).replaceFirst(SIGNED_URL_DOMAIN, "").split("/", 2);
      if (bucketAndKey.length == 2) {
        bucket = bucketAndKey[0];
        
        int keyFinishIndex = bucketAndKey[1].indexOf("?");

        if (keyFinishIndex > 0) {
          key = bucketAndKey[1].substring(0,keyFinishIndex);
        }
        else {
          key = bucketAndKey[1];
        }
        
        
        isValid = true;
      }
    }
  }
}
