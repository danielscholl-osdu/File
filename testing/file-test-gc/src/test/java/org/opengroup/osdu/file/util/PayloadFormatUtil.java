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

package org.opengroup.osdu.file.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.opengroup.osdu.file.constants.TestConstants;

public class PayloadFormatUtil {

  public static String updatePlaceholdersInMetadataInputPayload(String body) {
    body = body.replaceAll(TestConstants.TENANT_NAME_PLACEHOLDER,
            TestConstants.TENANT_NAME_PLACEHOLDER_VALUE)
        .replaceAll(TestConstants.ACL_VIEWERS_GROUP, TestConstants.ACL_VIEWERS_GROUP_VALUE)
        .replaceAll(TestConstants.ACL_OWNERS_GROUP, TestConstants.ACL_OWNERS_GROUP_VALUE)
        .replaceAll(TestConstants.CLOUD_DOMAIN, TestConstants.CLOUD_DOMAIN_VALUE)
        .replaceAll(TestConstants.LEGAL_TAGS, TestConstants.LEGAL_TAGS_VALUE);
    return body;
  }

  public static String updatePlaceholdersInDatasetInputPayload(String body, String recordId) {
    return body.replaceAll(TestConstants.REGISTRY_ID, recordId);
  }

  public static void updateFilePath(JsonElement jsonBody, String filePath) {
    jsonBody.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("DatasetProperties")
        .getAsJsonObject("FileSourceInfo").remove("FileSource");
    jsonBody.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("DatasetProperties")
        .getAsJsonObject("FileSourceInfo").addProperty("FileSource", filePath);
  }

  public static JsonElement replaceAncestryWithNewValue(JsonElement jsonBody, String ancestryVal) {
    JsonArray parentsVal = jsonBody.getAsJsonObject().getAsJsonObject("ancestry").getAsJsonArray("parents");
    parentsVal.remove(0);
    parentsVal.add(ancestryVal);
    return jsonBody;

  }

  public static JsonElement removeAncestry(JsonElement jsonBody) {
    jsonBody.getAsJsonObject().remove("ancestry");
    return jsonBody;
  }

}
