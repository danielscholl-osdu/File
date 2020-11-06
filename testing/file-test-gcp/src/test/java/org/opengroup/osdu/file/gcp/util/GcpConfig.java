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

public class GcpConfig {

  private static final String FILE_BUCKET = "";
  private static final String GCLOUD_PROJECT = "";
  private static final String GCP_DEPLOY_FILE = "";

  public static String getFileBucketName() {
    return getEnvironmentVariableOrDefaultValue("FILE_BUCKET", FILE_BUCKET);
  }

  public static String getProjectID() {
    return getEnvironmentVariableOrDefaultValue("GCLOUD_PROJECT", GCLOUD_PROJECT);
  }

  public static String getStorageAccount() {
    return getEnvironmentVariableOrDefaultValue("GCP_DEPLOY_FILE", GCP_DEPLOY_FILE);
  }

  private static String getEnvironmentVariableOrDefaultValue(String key, String defaultValue) {
    String environmentVariable = getEnvironmentVariable(key);
    if (environmentVariable == null) {
      environmentVariable = defaultValue;
    }
    return environmentVariable;
  }

  private static String getEnvironmentVariable(String propertyKey) {
    return System.getProperty(propertyKey, System.getenv(propertyKey));
  }


}
