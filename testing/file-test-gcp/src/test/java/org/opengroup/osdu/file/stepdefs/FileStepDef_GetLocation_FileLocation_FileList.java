package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.stepdefs.model.FileScope;
import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;
import org.opengroup.osdu.file.util.HttpClientFactory;
import org.opengroup.osdu.file.util.JsonUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java8.En;
import io.restassured.path.json.JsonPath;

public class FileStepDef_GetLocation_FileLocation_FileList implements En {

  @Inject
  private FileScope context;

  private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private String BODY_FORMAT_GETLOCATION_GETFILELOCATION = "{\"FileID\":\"<VALUE>\"}";

  public FileStepDef_GetLocation_FileLocation_FileList() {

    Given("I hit File service GetFileLocation API with non-existing file id", () -> {
      JsonElement jsonBody = new Gson().fromJson(getBodyString(CommonUtility.generateUniqueFileID()),
          JsonElement.class);
      HttpResponse response = postRequest(TestConstants.GET_FILE_LOCATION, CommonUtility.getValidHeader(),
          jsonBody);
      this.context.setHttpResponse(response);
    });

    Then("service should respond back with {string}", (String expectedReponseStatusCode) -> {
      String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
      assertTrue("Expected status - " + expectedReponseStatusCode + " ; Actual status code - " + actualStatusCode,
          expectedReponseStatusCode.equalsIgnoreCase(actualStatusCode));
    });

    Then("service should respond back with {string} or {string}", (String expectedResponseStatusCode, String alternateResponseCode) -> {
      String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
      assertTrue(expectedResponseStatusCode.equalsIgnoreCase(actualStatusCode)
          || alternateResponseCode.equalsIgnoreCase(actualStatusCode));
    });

    Given("I hit File service {string} with invalid partition id", (String apiName) -> {
      String apiEndPoint = getAPIEndPoint(apiName);
      HttpResponse response = postRequestWithEmptyBody(apiEndPoint,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton("invalidPartitionName"));
      this.context.setHttpResponse(response);
    });

    Given("I hit File service {string} without partition id", (String apiName) -> {
      String apiEndPoint = getAPIEndPoint(apiName);
      HttpResponse response = postRequestWithEmptyBody(apiEndPoint, CommonUtility.getHeaderWithoutPartiton());
      this.context.setHttpResponse(response);
    });

    Given("I hit File service {string} without auth token", (String apiName) -> {
      String apiEndPoint = getAPIEndPoint(apiName);
      HttpResponse response = postRequestWithEmptyBody(apiEndPoint, CommonUtility.getHeaderWithoutAuthToken());
      this.context.setHttpResponse(response);
    });

    Given("I hit File service {string} with invalid auth token", (String apiName) -> {
      String apiEndPoint = getAPIEndPoint(apiName);
      HttpResponse response = postRequestWithEmptyBody(apiEndPoint,
          CommonUtility.getHeaderWithInvaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1));
      this.context.setHttpResponse(response);
    });

    Given("I hit File service GetFileLocation API with {string}", (String BodyContent) -> {
      JsonElement jsonBody = null;
      if (BodyContent.equalsIgnoreCase("emptyReqBody")) {
        HttpResponse response = postRequestWithEmptyBody(TestConstants.GET_FILE_LOCATION,
            CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1));
        this.context.setHttpResponse(response);
      } else if (BodyContent.equalsIgnoreCase("invalidFileId")) {
        jsonBody = new Gson().fromJson(getBodyString("test"), JsonElement.class);
        HttpResponse response = postRequest(TestConstants.GET_FILE_LOCATION,
            CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1),
            jsonBody);
        this.context.setHttpResponse(response);
      }

    });

    Then("service should respond back with {string} and {string}",
        (String expectedReponseStatusCode, String expectedReponseMessage) -> {
          String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
          assertTrue("Expected status - " + expectedReponseStatusCode + " ; Actual status code - "
              + actualStatusCode, expectedReponseStatusCode.equalsIgnoreCase(actualStatusCode));
          String actualResponseMessage = new JsonPath(this.context.getHttpResponse().getBody())
              .get("message");
          assertTrue(
              "Expected message - " + expectedReponseMessage + " ; Actual message - "
                  + actualResponseMessage,
              expectedReponseMessage.equalsIgnoreCase(actualResponseMessage));
        });
    
    Then("service should respond back with {string} and error message {string}",
            (String expectedReponseStatusCode, String expectedReponseMessage) -> {
              String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
              assertTrue("Expected status - " + expectedReponseStatusCode + " ; Actual status code - "
                  + actualStatusCode, expectedReponseStatusCode.equalsIgnoreCase(actualStatusCode));
              String actualResponseMessage = new JsonPath(this.context.getHttpResponse().getBody())
                  .get("error.message");
              assertTrue(
                  "Expected message - " + expectedReponseMessage + " ; Actual message - "
                      + actualResponseMessage,
                  expectedReponseMessage.equalsIgnoreCase(actualResponseMessage));
            });

    Given("I hit File service GetLocation API with {string}", (String BodyContent) -> {
      JsonElement jsonBody = null;
      if (BodyContent.equalsIgnoreCase("invalid file location")) {
        jsonBody = new Gson().fromJson(getBodyString("/" + CommonUtility.generateUniqueFileID()),
            JsonElement.class);
      } else if (BodyContent.equalsIgnoreCase("fileId legth exceeding limit")) {
        jsonBody = new Gson().fromJson(getBodyString(CommonUtility.generateFileIDExceedingLegthLimit()),
            JsonElement.class);
      }
      HttpResponse response = postRequest(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1), jsonBody);
      this.context.setHttpResponse(response);
    });

    Given("I hit File service GetLocation API with existing file id", () -> {
      JsonElement fileIDJsonString = new Gson().fromJson(getBodyString(CommonUtility.generateUniqueFileID()),
          JsonElement.class);

      HttpResponse response = postRequest(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1),
          fileIDJsonString);
      String actualStatusCode = String.valueOf(response.getCode());
      assertTrue("Expected status - 200; Actual status code - " + actualStatusCode,
          "200".equalsIgnoreCase(actualStatusCode));

      response = postRequest(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1),
          fileIDJsonString);
      this.context.setHttpResponse(response);
    });

    Given("I hit File service GetFileList API with {string}", (String inputPayload) -> {
      JsonElement jsonBody = null;
      String body = this.context.getFileUtils().read(inputPayload);
      jsonBody = new Gson().fromJson(body, JsonElement.class);
      HttpResponse response = postRequest(TestConstants.GET_FILE_LIST,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1), jsonBody);
      this.context.setHttpResponse(response);
    });

    Given("I hit File service GetLocation API without File Id", () -> {
      HttpResponse response = postRequestWithEmptyBody(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1));
      this.context.setHttpResponse(response);
    });

    Then("service should respond back with {string} , File Id and Signed URL",
        (String expectedReponseStatusCode) -> {
          String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
          assertTrue("Expected status - " + expectedReponseStatusCode + " ; Actual status code - "
              + actualStatusCode, expectedReponseStatusCode.equalsIgnoreCase(actualStatusCode));
          String respBody = this.context.getHttpResponse().getBody();
          String responseFileId = new JsonPath(respBody).get("FileID");
          String responseLocationFileSource = new JsonPath(respBody).get("Location.FileSource").toString();
          String responseLocationSignedURL = new JsonPath(respBody).get("Location.SignedURL").toString();
          assertFalse(responseFileId.isEmpty() && responseLocationFileSource.isEmpty()
              && responseLocationSignedURL.isEmpty());
        });

    Given("I hit File service GetLocation API with a File Id", () -> {
      JsonElement jsonBody = new Gson().fromJson(getBodyString(CommonUtility.generateUniqueFileID()),
          JsonElement.class);
      HttpResponse response = postRequest(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1), jsonBody);
      this.context.setHttpResponse(response);
    });

    Given("I hit File service GetFileLocation API with same File Id", () -> {
      // call GetLocation to get a File Id
      JsonElement jsonBody = new Gson().fromJson(getBodyString(CommonUtility.generateUniqueFileID()),
          JsonElement.class);
      HttpResponse response = postRequest(TestConstants.GET_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1), jsonBody);
      String responseFileId = new JsonPath(response.getBody()).get("FileID");
      LOGGER.log(Level.INFO, "File Id generated by getLocation - " + responseFileId);
      // call GetFileLocation with above FileId
      JsonElement fileIdAsInputJson = new Gson().fromJson(responseFileId, JsonElement.class);
      response = postRequest(TestConstants.GET_FILE_LOCATION,
          CommonUtility.getHeaderWithVaidAuthorizationForPartiton(TestConstants.PRIVATE_TENANT1),
          fileIdAsInputJson);

      this.context.setHttpResponse(response);
    });

    Then("service should respond back with {string} and UnSigned URL", (String expectedReponseStatusCode) -> {
      String actualStatusCode = String.valueOf(this.context.getHttpResponse().getCode());
      assertTrue("Expected status - " + expectedReponseStatusCode + " ; Actual status code - " + actualStatusCode,
          expectedReponseStatusCode.equalsIgnoreCase(actualStatusCode));
      String respBody = this.context.getHttpResponse().getBody();
      String responseLocation = new JsonPath(respBody).get("Location").toString();

      assertFalse(responseLocation.isEmpty());
    });

  }

  private String getAPIEndPoint(String apiName) {
    String apiEndPoint = "";
    switch (apiName) {
      case "GetFileLocation":
        apiEndPoint = TestConstants.GET_FILE_LOCATION;
        break;

      case "GetFileList":
        apiEndPoint = TestConstants.GET_FILE_LIST;
        break;
    }
    return apiEndPoint;
  }

  private HttpResponse postRequest(String apiEndPoint, Map<String, String> headerMap, JsonElement jsonBody) {
    HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + apiEndPoint).body(jsonBody.toString())
        .httpMethod(HttpRequest.POST).requestHeaders(headerMap).build();
    HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
    return response;
  }

  private HttpResponse postRequestWithEmptyBody(String apiEndPoint, Map<String, String> headerMap) {
    HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + apiEndPoint).body("{}")
        .httpMethod(HttpRequest.POST).requestHeaders(headerMap).build();
    HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
    return response;
  }

  private String getBodyString(String content) {
    return BODY_FORMAT_GETLOCATION_GETFILELOCATION.replace("<VALUE>", content);
  }

}