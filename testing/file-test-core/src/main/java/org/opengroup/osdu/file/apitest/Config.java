/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 * Copyright © 2020 Amazon Web Services
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

  //File API Properties
  private static final String FILE_SERVICE_HOST = "";
  private static final String INTEGRATION_TESTER = "";
  private static final String NO_DATA_ACCESS_TESTER = "";
  private static final String TARGET_AUDIENCE = "";
  private static final String DATA_PARTITION_ID = "";
  private static final String USER_ID = "";


  //Delivery API Properties
  private static final String DEFAULT_SEARCH_HOST = "";
  private static final String DEFAULT_STORAGE_HOST = "";
  private static final String DEFAULT_LEGAL_HOST = "";
  private static final String DEFAULT_DATA_PARTITION_ID_TENANT1 = "";
  private static final String DEFAULT_DATA_PARTITION_ID_TENANT2 = "";
  private static final String DEFAULT_SEARCH_INTEGRATION_TESTER = "";

  private static final String DEFAULT_TARGET_AUDIENCE = "";

  private static final String DEFAULT_LEGAL_TAG = "";
  private static final String DEFAULT_OTHER_RELEVANT_DATA_COUNTRIES = "";

  private static final String DEFAULT_ENTITLEMENTS_DOMAIN = "";

  private static final String DEFAULT_TENANT = "opendes";

  // Common Methods

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

  public static String getTargetAudience() {
    return getEnvironmentVariableOrDefaultValue("TARGET_AUDIENCE", TARGET_AUDIENCE);
  }

  // File API Methods

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

  public static String getDataPartitionId() {
    return getEnvironmentVariableOrDefaultValue("DATA_PARTITION_ID", DATA_PARTITION_ID);
  }

  public static String getUserId() {
    return getEnvironmentVariableOrDefaultValue("USER_ID", USER_ID);
  }

  public static String getTimeZone() {
    return getEnvironmentVariableOrDefaultValue("TIME_ZONE", TIME_ZONE);
  }

  //Delivery API Methods
  public static String getOtherRelevantDataCountries() {
    return getEnvironmentVariableOrDefaultValue("OTHER_RELEVANT_DATA_COUNTRIES", DEFAULT_OTHER_RELEVANT_DATA_COUNTRIES);
  }

  public static String getLegalTag() {
      return getEnvironmentVariableOrDefaultValue("LEGAL_TAG", DEFAULT_LEGAL_TAG);
  }

  public static String getKeyValue() {
      return getEnvironmentVariableOrDefaultValue("SEARCH_INTEGRATION_TESTER", DEFAULT_SEARCH_INTEGRATION_TESTER);
  }

  public static String getDataPartitionIdTenant1() {
      return getEnvironmentVariableOrDefaultValue("DEFAULT_DATA_PARTITION_ID_TENANT1", DEFAULT_DATA_PARTITION_ID_TENANT1);
  }

  public static String getDataPartitionIdTenant2() {
      return getEnvironmentVariableOrDefaultValue("DEFAULT_DATA_PARTITION_ID_TENANT2", DEFAULT_DATA_PARTITION_ID_TENANT2);
  }

  public static String getSearchBaseURL() {
      return getEnvironmentVariableOrDefaultValue("SEARCH_HOST", DEFAULT_SEARCH_HOST);
  }

  public static String getStorageBaseURL() {
      return getEnvironmentVariableOrDefaultValue("STORAGE_HOST", DEFAULT_STORAGE_HOST);
  }

  public static String getLegalBaseURL() {
    return getEnvironmentVariableOrDefaultValue("LEGAL_HOST", DEFAULT_LEGAL_HOST);
}

  public static String getEntitlementsDomain() {
  return getEnvironmentVariableOrDefaultValue("ENTITLEMENTS_DOMAIN", DEFAULT_ENTITLEMENTS_DOMAIN);
  }

  public static String getTenant(){
      return getEnvironmentVariableOrDefaultValue("TENANT",DEFAULT_TENANT);
  }

}
