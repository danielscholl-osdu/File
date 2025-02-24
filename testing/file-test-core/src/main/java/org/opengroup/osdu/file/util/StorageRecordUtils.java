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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import org.opengroup.osdu.file.HttpClient;

import java.net.URL;
import java.util.Map;

import static org.opengroup.osdu.file.util.TestUtils.*;

public class StorageRecordUtils {

  // create metadata for registerDataSetId call
  public static String prepareFileMetadataRecord(String fileSource, String legalTag, String kind) {
    JsonObject record = getDefaultRecordWithDefaultData(fileSource, legalTag, kind);
    JsonArray records = new JsonArray();
    records.add(record);
    return records.toString();
  }

  // Send create record in storage.
  public static ClientResponse createMetadataRecord(HttpClient client, Map<String, String> headers, String recordMetadataBody) throws Exception {
    return client.sendExt(
        (new URL(getStorageUrl() + "records")).toString(),
        "PUT", headers, recordMetadataBody);
  }

  // create metadata for copy DMS call
  public static String convertStorageMetadataRecordToCopyDmsRequest(String recordMetadataBody) {
    JsonParser jsonParser = new JsonParser();
    JsonArray records = (JsonArray)jsonParser.parse(recordMetadataBody);

    JsonObject copyRequest = new JsonObject();
    copyRequest.add("datasetSources", records);

    return copyRequest.toString();
  }

  private static JsonObject getDefaultRecordWithDefaultData(String fileSource, String legalTag, String kind) {

    if(FILE_KIND.equals(kind)) {
      return getDefaultRecordForFileKind(fileSource, legalTag, kind);
    } else if(FILE_COLLECTION_KIND.equals(kind)) {
      return getDefaultRecordForFileCollectionKind(fileSource, legalTag, kind);
    }
    return null;
  }

  /*
  // Metadata expected for Files.
  "data": {
    "DatasetProperties": {
        "FileSourceInfo": {
            "FileSource": "{{fileSource}}"
        }
    }
  }
*/
  private static JsonObject getDefaultRecordForFileKind(String fileSource, String legalTag, String kind) {
    JsonObject data = new JsonObject();
    JsonObject datasetProperties = new JsonObject();
    JsonObject fileSourceInfo = new JsonObject();

    fileSourceInfo.addProperty("FileSource", fileSource);
    datasetProperties.add("FileSourceInfo", fileSourceInfo);
    data.add("DatasetProperties", datasetProperties);

    return getRecordWithInputData(legalTag, data, kind);
  }

  /*
  // Metadata expected for File Collection.
  "data": {
    "DatasetProperties": {
        "FileCollectionPath": ""
    }
  }
*/
  private static JsonObject getDefaultRecordForFileCollectionKind(String fileSource, String legalTag, String kind) {
    JsonObject data = new JsonObject();
    JsonObject datasetProperties = new JsonObject();

    data.add("DatasetProperties", datasetProperties);
    datasetProperties.addProperty("FileCollectionPath", fileSource);


    return getRecordWithInputData(legalTag, data, kind);
  }

  // add acl, legalTags and kind to record.
  private static JsonObject getRecordWithInputData(String legalTag, JsonObject data, String kind) {
    JsonObject record = getDefaultRecord(legalTag, kind);
    record.add("data", data);
    return record;
  }

  private static JsonObject getDefaultRecord(String legalTag, String kind) {
    JsonObject acl = new JsonObject();
    JsonArray acls = new JsonArray();
    acls.add(getAcl());
    acl.add("viewers", acls);
    acl.add("owners", acls);

    JsonArray tags = new JsonArray();
    tags.add(legalTag);

    JsonArray ordcJson = new JsonArray();
    ordcJson.add("BR");

    JsonObject legal = new JsonObject();
    legal.add("legaltags", tags);
    legal.add("otherRelevantDataCountries", ordcJson);

    JsonObject record = new JsonObject();

    record.addProperty("kind", kind);
    record.add("acl", acl);
    record.add("legal", legal);
    return record;
  }

  private static String getAcl() {
    return String.format("data.test1@%s", getAclSuffix());
  }

  private static String getAclSuffix() {
    String environment = getEnvironment();
    //build.gradle currently throws exception if a variable is set to empty or not set at all
    //workaround by setting it to an "empty" string to construct the url
    if (environment.equalsIgnoreCase("empty")) environment = "";
    if (!environment.isEmpty())
      environment = "." + environment;

    return String.format("%s%s.%s", getTenantName(), environment, getDomain());
  }

  private static String getStorageUrl() {
    String legalUrl = System.getProperty("STORAGE_HOST", System.getenv("STORAGE_HOST"));
    if (legalUrl == null || legalUrl.contains("-null")) {
      throw new RuntimeException("Storage host not configured");
    }
    return legalUrl;
  }

}
