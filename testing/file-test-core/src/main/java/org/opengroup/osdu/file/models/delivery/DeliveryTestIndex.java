// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.file.models.delivery;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;
import lombok.Data;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.file.HttpClient;
import org.opengroup.osdu.file.apitest.Config;
import org.opengroup.osdu.file.util.DeliveryFileHandler;

import javax.ws.rs.HttpMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.file.apitest.Config.*;

@Data
public class DeliveryTestIndex {
    private static final Logger LOGGER = Logger.getLogger(DeliveryTestIndex.class.getName());
    private String kind;
    private String index;
    private String mappingFile;
    private String recordFile;
    private int recordCount;
    private String schemaFile;
    private String[] dataGroup;
    private String[] viewerGroup;
    private String[] ownerGroup;
    private HttpClient httpClient;
    private Map<String, String> headers;
    private Gson gson = new Gson();

    public DeliveryTestIndex() {
    }

    public void setHttpClient(HttpClient httpClient) throws IOException {
        this.httpClient = httpClient;
        headers = getCommonHeader();
    }

    public void setupSchema() throws Exception {
        String schema = this.getStorageSchemaFromJson();
        ClientResponse clientResponse = this.httpClient.sendExt(getStorageBaseURL() + "schemas", HttpMethod.POST, headers,
                this.getStorageSchemaFromJson());
        if (clientResponse.getType() != null)
            LOGGER.info(String.format("Response status: %s, type: %s", clientResponse.getStatus(),
                    clientResponse.getType().toString()));
    }

    public void deleteSchema(String kind) throws Exception {
        ClientResponse clientResponse = this.httpClient.sendExt(getStorageBaseURL() + "schemas/" + kind, 
            HttpMethod.DELETE,
            headers,
            null);
        assertEquals(204, clientResponse.getStatus());
        if (clientResponse.getType() != null)
            LOGGER.info(String.format("Response status: %s, type: %s", clientResponse.getStatus(),
                    clientResponse.getType().toString()));
    }

    private String getRecordFile() {
        return String.format("%s.json", this.recordFile);
    }

    private String getMappingFile() {
        return String.format("%s.mapping", this.mappingFile);
    }

    private String getSchemaFile() {
        return String.format("%s.schema", this.schemaFile);
    }

    private List<Map<String, Object>> getRecordsFromTestFile() {
        try {
            String fileContent = DeliveryFileHandler.readFile(getRecordFile());
            List<Map<String, Object>> records = new Gson().fromJson(fileContent,
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());

            for (Map<String, Object> testRecord : records) {
                testRecord.put("kind", this.kind);
                testRecord.put("legal", generateLegalTag());
                testRecord.put("x-acl", dataGroup);
                Acl acl = Acl.builder().viewers(viewerGroup).owners(ownerGroup).build();
                testRecord.put("acl", acl);
            }
            return records;
        } catch (Exception ex) {
            throw new AssertionError(ex.getMessage());
        }
    }

    private String getIndexMappingFromJson() {
        try {
            String fileContent = DeliveryFileHandler.readFile(getMappingFile());
            JsonElement json = gson.fromJson(fileContent, JsonElement.class);
            return gson.toJson(json);
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private String getStorageSchemaFromJson() {
        try {
            String fileContent = DeliveryFileHandler.readFile(getSchemaFile());
            fileContent = fileContent.replaceAll("KIND_VAL", this.kind);
            JsonElement json = gson.fromJson(fileContent, JsonElement.class);
            return gson.toJson(json);
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private Legal generateLegalTag() {
        Legal legal = new Legal();
        Set<String> legalTags = new HashSet<>();
        legalTags.add(getLegalTag());
        legal.setLegaltags(legalTags);
        Set<String> otherRelevantCountries = new HashSet<>();
        otherRelevantCountries.add(getOtherRelevantDataCountries());
        legal.setOtherRelevantDataCountries(otherRelevantCountries);
        return legal;
    }

    public Map<String, String> getCommonHeader() throws IOException {

        String partition = Config.getDataPartitionIdTenant1();
        String accessToken = httpClient.getAccessToken();

        return getHeaders(partition, accessToken);
    }
    
    public static Map<String, String> overrideHeader(Map<String, String> currentHeaders, String... partitions) {
        String value = String.join(",", partitions);
        currentHeaders.put("data-partition-id", value);
        return currentHeaders;
    }

    public static Map<String, String> getHeaders(String dataPartition, String token) {
        Map<String, String> headers = new HashMap<>();
        System.out.println("Building headers here...");
        if (dataPartition != null && !dataPartition.isEmpty()) {
          System.out.println("Using Data partition: " + dataPartition);
          headers.put("data-partition-id", dataPartition);
        }
        if (token != null && !token.isEmpty()) {
          headers.put("Authorization", token);
        }
        return headers;
    }

}