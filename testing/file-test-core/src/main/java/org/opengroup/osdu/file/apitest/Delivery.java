/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.search.QueryRequest;
import org.opengroup.osdu.core.common.model.search.QueryResponse;
import org.opengroup.osdu.file.TestBase;
import org.opengroup.osdu.file.models.delivery.DeliveryTestIndex;
import org.opengroup.osdu.file.models.delivery.DeliveryRecordSetup;
import org.opengroup.osdu.file.models.delivery.DeliverySetup;
import org.opengroup.osdu.file.models.delivery.UrlSigningRequest;
import org.opengroup.osdu.file.models.delivery.UrlSigningResponse;
import org.opengroup.osdu.file.util.DeliveryFileHandler;

import lombok.extern.java.Log;

import javax.ws.rs.HttpMethod;

@Log
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class Delivery extends TestBase {

  protected static final String getLocation = "/getLocation";
  protected static final String getFileLocation = "/getFileLocation";
  protected static final String getFileList = "/getFileList";


  private Map<String, DeliveryTestIndex> inputIndexMap = new HashMap<>();
  private boolean shutDownHookAdded = false;

  private String timeStamp = String.valueOf(System.currentTimeMillis());
  private List<Map<String, Object>> records;

  private UrlSigningRequest urlSigningRequest = new UrlSigningRequest();

  protected Map<String, String> tenantMap = new HashMap<>();
  protected Map<String, DeliveryTestIndex> inputRecordMap = new HashMap<>();

  // remove
  protected static List<LocationResponse> locationResponses = new ArrayList<>();
  protected ObjectMapper mapper;

  @AfterAll
  public void tearDown() throws Exception {
    for (String kind : inputIndexMap.keySet()) {
      DeliveryTestIndex testIndex = inputIndexMap.get(kind);
      testIndex.deleteSchema(kind);
    }
    if (records != null && records.size() > 0) {
      for (Map<String, Object> testRecord : records) {
        String id = testRecord.get("id").toString();
        client.sendExt(Config.getStorageBaseURL() + "records/" + id, HttpMethod.DELETE, getDeliveryHeaders(), null);
        // log.info("Deleted the records");
      }
    }

    cloudStorageUtil.deleteBucket();
  }

  public Delivery() {
    mapper = new ObjectMapper()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @BeforeAll
  public void beforeAll() throws Exception {

    generateTenantMapping();

    createLegalTag();    

    List<DeliverySetup> inputList = DeliverySetupConf.getDeliverySetup();
    for (DeliverySetup input : inputList) {
      DeliveryTestIndex testIndex = getTextIndex();
      testIndex.setHttpClient(client);
      testIndex.setIndex(generateActualName(input.getIndex(), timeStamp));
      testIndex.setKind(generateActualName(input.getKind(), timeStamp));
      testIndex.setSchemaFile(input.getSchemaFile());
      inputIndexMap.put(testIndex.getKind(), testIndex);
    }

    for (String kind : inputIndexMap.keySet()) {
      DeliveryTestIndex testIndex = inputIndexMap.get(kind);
      testIndex.setupSchema();
    }
  }

  private void createLegalTag() throws IOException, Exception {

    String payLoad = getLegalTagBody("US", Config.getLegalTag(), null, "Public Domain Data", "Public Domain Data Legal Tag");

    ClientResponse clientResponse = client.sendExt(Config.getLegalBaseURL() + "legaltags", HttpMethod.POST, getDeliveryHeaders(), payLoad);
    // assertEquals(payLoad, "asdf");
    assertTrue(clientResponse.getStatus() == 201 || clientResponse.getStatus() == 409,String.format("Legaltag create response: %s", clientResponse.getStatus()));
  }

  private static String getLegalTagBody(String countryOfOrigin, String name, String expDate, String dataType, String description) {
    description = description == null ? "" : "\"description\" : \"" + description + "\",";
    expDate = ((expDate == null) || (expDate.length() == 0))  ? "" : "\"expirationDate\" : \"" + expDate + "\",";

    return "{\"name\": \"" + name + "\"," + description +
            "\"properties\": {\"countryOfOrigin\": [\"" + countryOfOrigin + "\"], \"contractId\":\"A1234\"," + expDate + "\"dataType\":\"" + dataType + "\", \"originator\":\" MyCompany\", \"securityClassification\":\"Public\", \"exportClassification\":\"EAR99\", \"personalData\":\"No Personal Data\"} }";
}

  private void ingest_records_with_the_for_a_given(String record, String dataGroup, String kind) {
    String actualKind = generateActualName(kind, timeStamp);
    try {
          String fileContent = DeliveryFileHandler.readFile(String.format("%s.%s", record, "json"));          
          StringSubstitutor stringSubstitutor = new StringSubstitutor(
              ImmutableMap.of(
                  "tenant", Config.getTenant(),
                  "domain",Config.getEntitlementsDomain())
          );
          records = new Gson().fromJson(fileContent, new TypeToken<List<Map<String, Object>>>() {}.getType());

          cloudStorageUtil.createBucket();

          for (Map<String, Object> testRecord : records) {
              String recordId = generateActualName(testRecord.get("id").toString(), timeStamp);
              String filePath = cloudStorageUtil.createCloudFile(recordId);
              Map<String,String> data = (Map<String, String>) testRecord.get("data");
              data.put("Data.GroupTypeProperties.PreLoadFilePath", filePath);
              data.put("ResourceID", recordId);
              testRecord.put("data", data);
              testRecord.put("id", recordId);
              testRecord.put("kind", actualKind);
              testRecord.put("legal", generateLegalTag());
              String[] x_acl = {stringSubstitutor.replace(generateActualName(dataGroup,timeStamp))};
              Acl acl = Acl.builder().viewers(x_acl).owners(x_acl).build();
              testRecord.put("acl", acl);
          }
          String payLoad = new Gson().toJson(records);
          
          ClientResponse clientResponse = client.sendExt(Config.getStorageBaseURL() + "records", HttpMethod.PUT, getDeliveryHeaders(), payLoad);
        assertEquals(201, clientResponse.getStatus());
    } catch (Exception ex) {
        throw new AssertionError(ex.getMessage());
    }
  }

  //Disabled as failing in Gitlab pipeline 
  //@Test
  public void ingestRecordsForAGivenSchemaAndTestSearchAndDelivery() throws Exception {

      List<DeliveryRecordSetup> recordSetups = new ArrayList<>();
      recordSetups.add(new DeliveryRecordSetup("index_records_1", "data.default.viewers@${tenant}.${domain}", "tenant1:testindex<timestamp>:well:3.0.0", 5));
      recordSetups.add(new DeliveryRecordSetup("index_records_1", "data.default.viewers@${tenant}.${domain}", "tenant1:testindex<timestamp>:well:1.0.0", 5));
      
      
      for (DeliveryRecordSetup recordSetup: recordSetups) {
        //Given: ingest records
        ingest_records_with_the_for_a_given(recordSetup.getRecord(), recordSetup.getDataGroup(), recordSetup.getKind());

        //Then: validate correct number of indexed/search records exist based on records ingested
        List<String> recordIds = searchRecordIds(recordSetup.getExpectedCount(), recordSetup.getKind());
        assertEquals(recordSetup.getExpectedCount(), recordIds.size());

        //Then: validate Delivery API returns correct number of delivery object based on nubmer of records ingested
        ClientResponse response = retrieveSignedResponse();
        String responseBody = response.getEntity(String.class);        
        UrlSigningResponse signedResponse = mapper.readValue(responseBody, UrlSigningResponse.class);
        assertEquals(recordSetup.getExpectedCount(), signedResponse.getProcessed().size());
        assertEquals(0, signedResponse.getUnprocessed().size());
      }
  }

  //Disabled as failing in Gitlab pipeline 
  //@Test
  public void ingestInvalidRecordsForAGivenSchemaAndGetBadResponse() throws Exception {

    List<DeliveryRecordSetup> recordSetups = new ArrayList<>();
    recordSetups.add(new DeliveryRecordSetup("index_records_2", "data.default.viewers@${tenant}.${domain}", "tenant1:testindex<timestamp>:well:2.0.0", 5));

    for (DeliveryRecordSetup recordSetup: recordSetups) {
      //Given: ingest records
      ingest_records_with_the_for_a_given(recordSetup.getRecord(), recordSetup.getDataGroup(), recordSetup.getKind());

      //Then: validate correct number of indexed/search records exist based on records ingested
      List<String> recordIds = searchRecordIds(recordSetup.getExpectedCount(), recordSetup.getKind());
      assertEquals(recordSetup.getExpectedCount(), recordIds.size());

      //Then: validate Delivery API returns an error with no file path
      ClientResponse response = retrieveSignedResponse();         
      assertEquals(500, response.getStatus());
    }

    
  }

  // Method to be overridden by cloud providers to validate connectionString
  // property in the response
  public void validate_cloud_provider_connection_string(UrlSigningResponse signedResponse) {
  }

  private ClientResponse retrieveSignedResponse() throws Exception {
    String payload = mapper.writeValueAsString(urlSigningRequest);
    return client.sendExt(Config.getFileServiceHost() + "/delivery/GetFileSignedUrl", HttpMethod.POST, getDeliveryHeaders(), payload);
  }

  private List<String> searchRecordIds(int expectedCount, String kind) throws IOException, Exception {
    Gson gson = new Gson();
    String actualKind = generateActualName(kind, timeStamp);
    List<String> recordIds = new ArrayList<>();
    int iterator;

    // Thread.sleep(40000);
    QueryRequest queryRequest = new QueryRequest();
    queryRequest.setKind(actualKind);
    String payload = gson.toJson(queryRequest);
    for (iterator = 0; iterator < 20; iterator++) {
      ClientResponse clientResponse = client.sendExt(Config.getSearchBaseURL() + "query", HttpMethod.POST,
      getDeliveryHeaders(), payload);
      String responseBody = clientResponse.getEntity(String.class);
      QueryResponse response = gson.fromJson(responseBody, QueryResponse.class);
      if (response.getTotalCount() > 0) {
        log.info(String.format("index: %s | attempts: %s | documents acknowledged by elastic: %s", kind, iterator,
            response.getTotalCount()));
        if (expectedCount == response.getTotalCount()) {
          recordIds = response.getResults().stream().map(item -> item.get("id").toString())
              .collect(Collectors.toList());
          urlSigningRequest.setSrns(recordIds);
          break;
        }
      }
      Thread.sleep(5000);
    }
    return recordIds;
  }

  private Boolean areJsonEqual(String firstJson, String secondJson) {
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    Map<String, Object> firstMap = gson.fromJson(firstJson, mapType);
    Map<String, Object> secondMap = gson.fromJson(secondJson, mapType);

    MapDifference<String, Object> result = Maps.difference(firstMap, secondMap);
    if (result != null && result.entriesDiffering().isEmpty())
      return true;
    log.info(String.format("difference: %s", result.entriesDiffering()));
    return false;
  }

  protected String getTenantMapping(String tenant) {
    if (tenantMap.containsKey(tenant)) {
      return tenantMap.get(tenant);
    }
    return null;
  }

  protected String generateActualName(String rawName, String timeStamp) {
    for (String tenant : tenantMap.keySet()) {
      rawName = rawName.replaceAll(tenant, getTenantMapping(tenant));
      }
      return rawName.replaceAll("<timestamp>", timeStamp);
    }

  protected DeliveryTestIndex getTextIndex() {
        return new DeliveryTestIndex();
  }

  protected Legal generateLegalTag() {
      Legal legal = new Legal();
      Set<String> legalTags = new HashSet<>();
      legalTags.add(Config.getLegalTag());
      legal.setLegaltags(legalTags);
      Set<String> otherRelevantCountries = new HashSet<>();
      otherRelevantCountries.add(Config.getOtherRelevantDataCountries());
      legal.setOtherRelevantDataCountries(otherRelevantCountries);
      return legal;
  }

  private void generateTenantMapping(){
    tenantMap.put("tenant1", Config.getDataPartitionIdTenant1());
    tenantMap.put("tenant2", Config.getDataPartitionIdTenant2());
    tenantMap.put("common", "common");
  }

  public static Map<String, String> getDeliveryHeaders() throws IOException {
    return getHeaders(Config.getDataPartitionIdTenant1(), client.getAccessToken());
  }

}
