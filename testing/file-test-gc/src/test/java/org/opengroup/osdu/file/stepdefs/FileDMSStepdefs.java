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

package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.stepdefs.model.FileScope;
import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;
import org.opengroup.osdu.file.util.AuthUtil;
import org.opengroup.osdu.file.util.HttpClientFactory;
import org.opengroup.osdu.file.util.JsonUtils;
import org.opengroup.osdu.file.util.PayloadFormatUtil;

public class FileDMSStepdefs {

  private final ObjectMapper mapper = new ObjectMapper();

  @Inject
  private FileScope context;

  @Before
  public void setUp() throws Exception {
    if (this.context.getToken() == null) {
      String token = new AuthUtil().getToken();
      this.context.setToken(token);
    }

    if (this.context.getAuthHeaders() == null) {
      Map<String, String> authHeaders = new HashMap<String, String>();
      authHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken());
      authHeaders.put(TestConstants.DATA_PARTITION_ID, TestConstants.PRIVATE_TENANT1);
      authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
      this.context.setAuthHeaders(authHeaders);
    }
  }

  @When("I send request for storage instructions i should receive valid response")
  public void iSendRequestForStorageInstructionsIShouldReceiveValidResponse() throws IOException {
    HttpRequest httpRequest = HttpRequest.builder()
        .url(TestConstants.HOST + TestConstants.DMS_GET_STORAGE_INSTRUCTIONS_ENDPOINT)
        .httpMethod(HttpRequest.POST)
        .requestHeaders(this.context.getAuthHeaders()).build();

    HttpResponse httpResponse = HttpClientFactory.getInstance().send(httpRequest);
    assertEquals(HttpStatusCodes.STATUS_CODE_OK, httpResponse.getCode());
    this.context.setHttpResponse(httpResponse);
    StorageInstructionsResponse storageInstructions =
        mapper.readValue(httpResponse.getBody(), StorageInstructionsResponse.class);
    this.context.setFileSource(
        storageInstructions.getStorageLocation().get("fileSource").toString());
    this.context.setSignedUrl(storageInstructions.getStorageLocation().get("signedUrl").toString());
  }

    @Then("I should be able to upload file from {string} with provided instruction")
    public void iShouldBeAbleToUploadFileWithProvidedInstruction(String inputFilePath) throws IOException {
        int responseCode = this.context.getFileUtils().uploadFileBySignedUrl(this.context.getSignedUrl(),
                inputFilePath);
        // Both 200 and 201 response codes indicate success API calls.
        assertTrue(
                responseCode == HttpStatusCodes.STATUS_CODE_CREATED || responseCode == HttpStatusCodes.STATUS_CODE_OK);

    }

  @Then("I should be able to register metadata with {string} and uploaded file")
  public void iShouldBeAbleToRegisterMetadataWithUploadedFile(String metadataInputPayload)
      throws IOException {
    String body = this.context.getFileUtils().readFromLocalFilePath(metadataInputPayload);
    body = PayloadFormatUtil.updatePlaceholdersInMetadataInputPayload(body);
    JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
    String filepath = this.context.getFileSource();
    PayloadFormatUtil.updateFilePath(jsonBody, filepath);

    HttpRequest httpRequest = HttpRequest.builder()
        .url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
        .httpMethod(HttpRequest.POST)
        .body(jsonBody.toString())
        .requestHeaders(this.context.getAuthHeaders()).build();

    HttpResponse httpResponse = HttpClientFactory.getInstance().send(httpRequest);
    assertEquals(HttpStatusCodes.STATUS_CODE_CREATED, httpResponse.getCode());
    String recordId = JsonUtils.getAsJsonPath(httpResponse.getBody()).get(TestConstants.ID)
        .toString();
    this.context.setId(recordId);
  }

  @Then("I should be able request for retrieval instructions for uploaded file by {string}")
  public void iShouldBeAbleRequestForRetrievalInstructionsForUploadedFileByMetadataId(
      String datasetRegistryInputPayload)
      throws IOException {
    String recordId = this.context.getId();
    String getRetrievalInstructionsBody =
        this.context.getFileUtils().readFromLocalFilePath(datasetRegistryInputPayload);
    String requestBody =
        PayloadFormatUtil.updatePlaceholdersInDatasetInputPayload(getRetrievalInstructionsBody,
            recordId);
    HttpRequest httpRequest = HttpRequest.builder()
        .url(TestConstants.HOST + TestConstants.DMS_GET_RETRIEVAL_INSTRUCTIONS_ENDPOINT)
        .httpMethod(HttpRequest.POST)
        .body(requestBody)
        .requestHeaders(this.context.getAuthHeaders()).build();
    HttpResponse httpResponse = HttpClientFactory.getInstance().send(httpRequest);
    assertEquals(HttpStatusCodes.STATUS_CODE_OK, httpResponse.getCode());

    RetrievalInstructionsResponse retrievalInstructionsResponse =
        new Gson().fromJson(httpResponse.getBody(), RetrievalInstructionsResponse.class);

    DatasetRetrievalProperties retrievalProperties = retrievalInstructionsResponse.getDatasets()
        .stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Get retrieval instructions empty datasets"));
    this.context.setSignedUrl(
        retrievalProperties.getRetrievalProperties().get("signedUrl").toString());
  }

  @Then("I should be able retrieve file by provided instructions, and downloaded files is same as {string}")
  public void i_should_be_able_retrieve_file_by_provided_instructions_and_downloaded_files_is_same_as(
      String inputFilePath)
      throws IOException {
    String expectedFileContent = this.context.getFileUtils().readFromLocalFilePath(inputFilePath);
    URL downloadUrl = new URL(this.context.getSignedUrl());
    String actualFileContent = this.context.getFileUtils().readFileBySignedUrl(downloadUrl);
    assertEquals(expectedFileContent, actualFileContent);
  }
}
