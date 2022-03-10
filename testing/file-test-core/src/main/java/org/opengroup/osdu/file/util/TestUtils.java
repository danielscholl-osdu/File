/*
 * Copyright 2021 Microsoft Corporation
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

package org.opengroup.osdu.file.util;

public class TestUtils {

  public static final String FILE_KIND = "osdu:wks:dataset--File.Generic:1.0.0";
  public static final String FILE_COLLECTION_KIND = "osdu:wks:dataset--FileCollection.Generic:1.0.0";

  public static String getTenantName() {
    return System.getProperty("TENANT_NAME", System.getenv("TENANT_NAME"));
  }

  public static String getDomain() {
    return System.getProperty("DOMAIN", System.getenv("DOMAIN"));
  }

  public static String getEnvironment() {
    return System.getProperty("DEPLOY_ENV", System.getenv("DEPLOY_ENV"));
  }
}
