package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java8.En;

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
					String body = this.context.getFileUtils().read(inputPayload);
					body = updatePlaceholdersInInputPayload(body);
					JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
					String filepath = this.context.getFileSource();
					updateFilePath(jsonBody, filepath);
					HttpResponse response = postRequest(jsonBody, tenant);
					this.context.setHttpResponse(response);
					setId();
				});

		Given("I hit File metadata service POST API with {string} and data-partition-id as {string} for validations",
				(String inputPayload, String tenant) -> {
					tenant = CommonUtil.selectTenant(tenant);
					String body = this.context.getFileUtils().read(inputPayload);
					body = updatePlaceholdersInInputPayload(body);
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
					String body = this.context.getFileUtils().read(ResponseToBeVerified);
					gsn = new Gson();
					JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
					JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
					assertEquals(expectedData.toString(), responseMsg.toString());
				});
		
		Then("I update ancestry value with {string} and data-partition-id as {string}",
				(String inputPayload, String tenant) -> {
					tenant = CommonUtil.selectTenant(tenant);
					String body = this.context.getFileUtils().read(inputPayload);
					body = updatePlaceholdersInInputPayload(body);
					JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
					jsonBody = removeAncestry(jsonBody);
					String filepath = this.context.getFileSource();
					updateFilePath(jsonBody, filepath);
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
					jsonBody = replaceAncestryWithNewValue(jsonBody, ancestryVal);
				});
	}


	private JsonElement replaceAncestryWithNewValue(JsonElement jsonBody, String ancestryVal) {
				JsonArray parentsVal = jsonBody.getAsJsonObject().getAsJsonObject("ancestry").getAsJsonArray("parents");
				parentsVal.remove(0);
				parentsVal.add(ancestryVal);
		return jsonBody;
				
	}


	private void getVersionValue(JsonElement jsonBody, String filePath) {
		jsonBody.getAsJsonObject().getAsJsonObject("data").remove("FileSource");
		jsonBody.getAsJsonObject().getAsJsonObject("data").addProperty("FileSource", filePath);
	}

	private String updatePlaceholdersInInputPayload(String body) {
		body = body.replaceAll(TestConstants.TENANT_NAME_PLACEHOLDER, TestConstants.TENANT_NAME_PLACEHOLDER_VALUE)
				.replaceAll(TestConstants.ACL_VIEWERS_GROUP, TestConstants.ACL_VIEWERS_GROUP_VALUE)
				.replaceAll(TestConstants.ACL_OWNERS_GROUP, TestConstants.ACL_OWNERS_GROUP_VALUE)
				.replaceAll(TestConstants.CLOUD_DOMAIN, TestConstants.CLOUD_DOMAIN_VALUE)
				.replaceAll(TestConstants.LEGAL_TAGS, TestConstants.LEGAL_TAGS_VALUE);
		return body;
	}

	private HttpResponse postRequest(JsonElement jsonBody, String tenant) {
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
				.body(jsonBody.toString()).httpMethod(HttpRequest.POST).requestHeaders(this.context.getAuthHeaders())
				.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		return response;
	}
	
	private HttpResponse getStorageRequest(String id) {
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.STORAGE_HOST + id)
				.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders())
				.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		return response;
	}

	private void commonAssertion() {
		assertNotNull(getResponseValue(TestConstants.ID));
	}

	private String getExpectedValue(JsonObject jsonBody, String valueToBeRetrieved) {
		String value;
		value = jsonBody.get(valueToBeRetrieved).toString();
		return value.substring(1, value.length() - 1);
	}

	private String getResponseValue(String responseAttribute) {
		return JsonUtils.getAsJsonPath(this.context.getHttpResponse().getBody().toString()).get(responseAttribute)
				.toString();
	}

	private void updateFilePath(JsonElement jsonBody, String filePath) {
		jsonBody.getAsJsonObject().getAsJsonObject("data").remove("FileSource");
		jsonBody.getAsJsonObject().getAsJsonObject("data").addProperty("FileSource", filePath);
	}
	
	private JsonElement removeAncestry(JsonElement jsonBody) {
		jsonBody.getAsJsonObject().remove("ancestry");
		return jsonBody;

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