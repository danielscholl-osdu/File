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

package org.opengroup.osdu.file.apitest;

import java.util.TimeZone;

public class Config {

  private static final String FILE_SERVICE_HOST = "";
  private static final String INTEGRATION_TESTER = "";
  private static final String NO_DATA_ACCESS_TESTER = "";
  private static final String TARGET_AUDIENCE = "";
  private static final String DATA_PARTITION_ID = "";
  private static final String USER_ID = "";
  //  Storage time zone id, example = UTC+0
  private static final String TIME_ZONE = TimeZone.getDefault().getID();

  public static String getFileServiceHost() {
    return getEnvironmentVariableOrDefaultValue("FILE_SERVICE_HOST", FILE_SERVICE_HOST);
  }

  public static String getIntegrationTester() {
    return getEnvironmentVariableOrDefaultValue("INTEGRATION_TESTER", INTEGRATION_TESTER);
  }

  public static String getNoAccessTester() {
    return getEnvironmentVariableOrDefaultValue("NO_DATA_ACCESS_TESTER", NO_DATA_ACCESS_TESTER);
  }

  public static String getTargetAudience() {
    return getEnvironmentVariableOrDefaultValue("TARGET_AUDIENCE", TARGET_AUDIENCE);
  }

  public static String getDataPartitionId() {
    return getEnvironmentVariableOrDefaultValue("DATA_PARTITION_ID", DATA_PARTITION_ID);
  }

  public static String getUserId() {
    return getEnvironmentVariableOrDefaultValue("USER_ID", USER_ID);
  }

  public static String getTimeZone() {
    return getEnvironmentVariableOrDefaultValue("TIME_ZONE", TIME_ZONE);
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
