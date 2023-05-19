/*
 *  Copyright 2020-2022 Google LLC
 *  Copyright 2020-2022 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.cucumber.java8.En;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.stepdefs.model.FileScope;
import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;
import org.opengroup.osdu.file.util.CommonUtil;
import org.opengroup.osdu.file.util.HttpClientFactory;
import org.opengroup.osdu.file.util.JsonUtils;
import org.opengroup.osdu.file.util.PayloadFormatUtil;

public class FileStepDef_POST implements En {

  @Inject
  private FileScope context;

  static String[] GetListBaseFilterArray;
  static String[] GetListVersionFilterArray;
  String queryParameter;
  Gson gsn = null;

  private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  List<HashMap<String, String>> list_fileDMSParameterMap = new ArrayList<HashMap<String, String>>();

  public FileStepDef_POST() {

    Given("I hit File service metadata service POST API with {string} and data-partition-id as {string}",
        (String inputPayload, String tenant) -> {
          tenant = CommonUtil.selectTenant(tenant);
          String body = this.context.getFileUtils().readFromLocalFilePath(inputPayload);
          body = PayloadFormatUtil.updatePlaceholdersInMetadataInputPayload(body);
          JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
          String filepath = this.context.getFileSource();
          PayloadFormatUtil.updateFilePath(jsonBody, filepath);
          HttpResponse response = postRequest(jsonBody, tenant);
          this.context.setHttpResponse(response);
          setId();
        });

    Given("I hit File metadata service POST API with {string} and data-partition-id as {string} for validations",
        (String inputPayload, String tenant) -> {
          tenant = CommonUtil.selectTenant(tenant);
          String body = this.context.getFileUtils().readFromLocalFilePath(inputPayload);
          body = PayloadFormatUtil.updatePlaceholdersInMetadataInputPayload(body);
          JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
          HttpResponse response = postRequest(jsonBody, tenant);
          this.context.setHttpResponse(response);
        });

    Then("Service should respond back with {string}", (String reponseStatusCode) -> {
      HttpResponse response = this.context.getHttpResponse();
      if (response != null) {
        assertEquals(reponseStatusCode, String.valueOf(response.getCode()));
        commonAssertion();
      }
    });

    Then("Service should respond back with error {string} and {string}",
        (String ReponseStatusCode, String ResponseToBeVerified) -> {
          HttpResponse response = this.context.getHttpResponse();
          assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
          String body = this.context.getFileUtils().readFromLocalFilePath(ResponseToBeVerified);
          gsn = new Gson();
          JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
          JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
          assertEquals(expectedData.toString(), responseMsg.toString());
        });

    Then("I update ancestry value with {string} and data-partition-id as {string}",
        (String inputPayload, String tenant) -> {
          tenant = CommonUtil.selectTenant(tenant);
          String body = this.context.getFileUtils().readFromLocalFilePath(inputPayload);
          body = PayloadFormatUtil.updatePlaceholdersInMetadataInputPayload(body);
          JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
          jsonBody = PayloadFormatUtil.removeAncestry(jsonBody);
          String filepath = this.context.getFileSource();
          PayloadFormatUtil.updateFilePath(jsonBody, filepath);
          HttpResponse response = postRequest(jsonBody, tenant);
          this.context.setHttpResponse(response);
          setId();
          String id = this.context.getId();
          response = getStorageRequest(id);
          this.context.setHttpResponse(response);
          setVersion();
          String version = this.context.getVersion();
          String ancestryVal = id + ":" + version;
          jsonBody = new Gson().fromJson(body, JsonElement.class);
          jsonBody = PayloadFormatUtil.replaceAncestryWithNewValue(jsonBody, ancestryVal);
        });
  }

  private HttpResponse postRequest(JsonElement jsonBody, String tenant) {
    HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
        .body(jsonBody.toString()).httpMethod(HttpRequest.POST).requestHeaders(this.context.getAuthHeaders())
        .build();
    HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
    return response;
  }

  private HttpResponse getStorageRequest(String id) {
    HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.STORAGE_HOST + TestConstants.STORAGE_GET_ENDPOINT + id)
        .httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders())
        .build();
    HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
    return response;
  }

  private void commonAssertion() {
    assertNotNull(getResponseValue(TestConstants.ID));
  }

  private String getResponseValue(String responseAttribute) {
    return JsonUtils.getAsJsonPath(this.context.getHttpResponse().getBody()).get(responseAttribute)
        .toString();
  }

  private void setId() throws IOException {
    String response = this.context.getHttpResponse().getBody();
    gsn = new Gson();
    JsonObject root = gsn.fromJson(response, JsonObject.class);
    this.context.setId(root.get("id").getAsString());
  }

  private void setVersion() throws IOException {
    String response = this.context.getHttpResponse().getBody();
    gsn = new Gson();
    JsonObject root = gsn.fromJson(response, JsonObject.class);
    this.context.setVersion(root.get("version").getAsString());
  }
}
