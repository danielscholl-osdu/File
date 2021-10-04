package org.opengroup.osdu.file.stepdefs;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.stepdefs.model.FileScope;
import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;
import org.opengroup.osdu.file.util.AuthUtil;
import org.opengroup.osdu.file.util.CommonUtil;
import org.opengroup.osdu.file.util.HttpClientFactory;
import org.opengroup.osdu.file.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.cucumber.java8.En;
import static org.junit.Assert.*;

public class FileStepDef_GET implements En {

	@Inject
	private FileScope context;

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	Gson gsn = null;
	Gson gsnActual = null;

	public FileStepDef_GET() {

		Given("I generate user token and set request headers with {string}", (String tenant) -> {
			if (this.context.getToken() == null) {
				String token = new AuthUtil().getToken();
				this.context.setToken(token);
			}

			if (this.context.getAuthHeaders() == null) {
				Map<String, String> authHeaders = new HashMap<String, String>();
				authHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken());
				authHeaders.put(TestConstants.DATA_PARTITION_ID, CommonUtil.selectTenant(tenant));
				authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
				this.context.setAuthHeaders(authHeaders);
			}
		});

		Given("I hit File service GET API with missing or invalid {string} and {string}",
				(String header, String headerValue) -> {

					if (TestConstants.AUTHORIZATION.equals(header))
						this.context.getAuthHeaders().put(TestConstants.AUTHORIZATION, headerValue);

					if (TestConstants.DATA_PARTITION_ID.equals(header))
						this.context.getAuthHeaders().put(TestConstants.DATA_PARTITION_ID, headerValue);

					HttpRequest httpRequest = HttpRequest.builder()
							.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_UPLOAD_ENDPOINT)
							.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();

					HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
					this.context.setHttpResponse(response);

					LOGGER.log(Level.INFO, "resp - " + response.toString());
				});

		Given("I hit File service GET uploadURL API", () -> {

			HttpRequest httpRequest = HttpRequest.builder()
					.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_UPLOAD_ENDPOINT).httpMethod(HttpRequest.GET)
					.requestHeaders(this.context.getAuthHeaders()).build();

			HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
			this.context.setHttpResponse(response);
			setNewFileSourceValue();
			setUploadSignedUrl();

			assertEquals("200", String.valueOf(response.getCode()));
			LOGGER.log(Level.INFO, "resp - " + response.toString());
		});

		When("I try to use signed url after expiration period {string} and file path {string}",
				(String expiredURL, String inputFilePath) -> {
					int code = this.context.getFileUtils().uploadFileBySignedUrl(expiredURL, inputFilePath);
					this.context.setResponseCode(new Integer(code).toString());

				});

		When("I try to use signed url within expiration period and file path {string}", (String inputFilePath) -> {

			String response = this.context.getHttpResponse().getBody();
			LocationResponse signedURLResp = JsonUtils.getPojoFromJSONString(LocationResponse.class, response);
			assertNotNull(signedURLResp.getLocation().get("SignedURL"));
			int code = this.context.getFileUtils().uploadFileBySignedUrl(signedURLResp.getLocation().get("SignedURL"), inputFilePath);
			this.context.setResponseCode(new Integer(code).toString());
		});

		Then("service should respond back with a valid {string} and upload input file from {string}",
				(String respCode, String inputFilePath) -> {
					verifySuccessfulGetSignedURLResponse(respCode, inputFilePath);
				});

		Then("download service should respond back with a valid {string}", (String respCode) -> {
			validateResponseCode(respCode);
			String response = this.context.getHttpResponse().getBody();
			DownloadUrlResponse signedURLResp = JsonUtils.getPojoFromJSONString(DownloadUrlResponse.class, response);
			assertNotNull(context.getSignedUrl());
		});

		Then("metadata service should respond back with a valid {string}", (String respCode) -> {
			validateResponseCode(respCode);
			String response = this.context.getHttpResponse().getBody();
			RecordVersion metadataResp = JsonUtils.getPojoFromJSONString(RecordVersion.class, response);
		});

		Then("service should respond back with error {string} and {string}", (String errorCode, String errorMsg) -> {
			verifyFailedResponse(errorCode, errorMsg);
		});

		Then("service should respond back with error {string} or {string} and {string}",
				(String errorCode, String alternateErrorCode, String errorMsg) -> {
					validateResponseCode(errorCode, alternateErrorCode);
				});

		Then("service should respond back with error code {string}", (String errorCode) -> {
			assertEquals(errorCode, this.context.getResponseCode());
		});

		Given("I hit File service GET download signed API with a valid Id", () -> {
			String id = this.context.getId();
			HttpRequest httpRequest = HttpRequest.builder()
					.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + id
							+ TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT2)
					.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();

			HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
			this.context.setHttpResponse(response);
			LOGGER.log(Level.INFO, "resp - " + response.toString());
		});

		Given("I hit File service GET metadata signed API with a valid Id", () -> {
			String id = this.context.getId();
			HttpRequest httpRequest = HttpRequest.builder()
					.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + id
							+ TestConstants.GET_METADATA_ENDPOINT2)
					.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
			HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
			this.context.setHttpResponse(response);
			LOGGER.log(Level.INFO, "resp - " + response.toString());
		});

		Given("I hit File service GET metadata signed API with an {string}", (String invalidId) -> {
			String id = this.context.getId();
			HttpRequest httpRequest = HttpRequest.builder()
					.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + invalidId
							+ TestConstants.GET_METADATA_ENDPOINT2)
					.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
			HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
			this.context.setHttpResponse(response);
			LOGGER.log(Level.INFO, "resp - " + response.toString());
		});

		When("I hit signed url to download a file within expiration period at {string}", (String outputFilePath) -> {
			String response = this.context.getHttpResponse().getBody();
			String downLoadUrl = this.context.getSignedUrl();
			this.context.getFileUtils().readFileBySignedUrlAndWriteToLocalFile(downLoadUrl, outputFilePath);
		});

		When("content of the file uploaded {string} and downloaded {string} files is same",
				(String outputFilePath, String inputFilePath) -> {
					compareFileContent(outputFilePath, inputFilePath);
				});

		When("I hit File service GET download signed API with a valid Id and {string}",
				(String expiryTimeInMinutes) -> {
					String id = this.context.getId();

					Map<String, String> queryParam = new HashMap<>();
					queryParam.put(TestConstants.EXPIRY_TIME_PARA_NAME, expiryTimeInMinutes);

					HttpRequest httpRequest = HttpRequest.builder()
							.url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + id
									+ TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT2)
							.queryParams(queryParam).httpMethod(HttpRequest.GET)
							.requestHeaders(this.context.getAuthHeaders()).build();

					HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
					this.context.setHttpResponse(response);
				});

		Then("I should be able to download the file within expiry period", () -> {
			String response = this.context.getHttpResponse().getBody();
			DownloadUrlResponse signedURLResp = JsonUtils.getPojoFromJSONString(DownloadUrlResponse.class, response);
			assertNotNull("No download url returned by service.", signedURLResp.getSignedUrl());
			URL url = new URL(signedURLResp.getSignedUrl());
			URLConnection conn = url.openConnection();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				assertNotNull("No content present in the file downloaded using download url.",
						readDownloadedFileContent(br));
			} catch (IOException ex) {
				LOGGER.log(Level.INFO, "Exception accessing download url - " + ex.getMessage());
				fail("Failed to download the file within expiry time");
			}
		});

		And("I should not be able to download the file after {string}", (String expiryTimeInMinutes) -> {
			// wait for timeout to expire
			CommonUtility.customStaticWait_Max_5_Minutes(
					Long.valueOf(expiryTimeInMinutes.substring(0, expiryTimeInMinutes.length() - 1)));

			String response = this.context.getHttpResponse().getBody();
			DownloadUrlResponse signedURLResp = JsonUtils.getPojoFromJSONString(DownloadUrlResponse.class, response);
			URL url = new URL(signedURLResp.getSignedUrl());
			URLConnection conn = url.openConnection();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				LOGGER.log(Level.INFO, "Value of line read from file - " + readDownloadedFileContent(br));
				fail("File could be downloaded even after expiry time");
			} catch (IOException ex) {
				LOGGER.log(Level.INFO, "As expected, Exception occured accessing download url post expiry period - "
						+ ex.getMessage());
			}
		});

	}

	private String readDownloadedFileContent(BufferedReader br) throws IOException {
		String inputLine;
		StringBuilder downloadedFile = new StringBuilder();
		while ((inputLine = br.readLine()) != null) {
			downloadedFile.append(inputLine);
		}
		return new String(downloadedFile);
	}

	private void compareFileContent(String outputFilePath, String inputFilePath) throws IOException {
		String outputContent = this.context.getFileUtils().readFromLocalFilePath(outputFilePath);
		String inputContent = this.context.getFileUtils().readFromLocalFilePath(inputFilePath);
		assertTrue(outputContent.contentEquals(inputContent));
	}

	private void verifySuccessfulGetSignedURLResponse(String responseCode, String inputFilePath)
			throws InterruptedException, IOException {
		validateResponseCode(responseCode);

		String response = this.context.getHttpResponse().getBody();
		LocationResponse signedURLResp = JsonUtils.getPojoFromJSONString(LocationResponse.class, response);

		assertNotNull(signedURLResp);
		assertNotNull(signedURLResp.getLocation().get("SignedURL"));
		assertNotNull(signedURLResp.getLocation().get("FileSource"));

		int code = 0;
		try {
			code = this.context.getFileUtils().uploadFileBySignedUrl(signedURLResp.getLocation().get("SignedURL"), inputFilePath);
		} catch (IOException e) {
			fail("Fail to call signed URL because of message=" + e.getMessage());
		}

		// Both 200 and 201 response codes indicate success in PUT calls.
		assertTrue(code == 200 || code == 201);
	}

	private void validateResponseCode(String responseCode) {
		int respCode = this.context.getHttpResponse().getCode();
		assertEquals(responseCode, String.valueOf(respCode));
	}

	private void validateResponseCode(String responseCode, String alternateResponseCode) {
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertTrue(responseCode.equals(String.valueOf(response.getCode()))
					|| alternateResponseCode.equals(String.valueOf(response.getCode())));
		}
	}

	private void setNewFileSourceValue() throws IOException {
		String response = this.context.getHttpResponse().getBody();
		gsn = new Gson();
		JsonObject root = gsn.fromJson(response, JsonObject.class);
		this.context.setFileSource(root.get("Location").getAsJsonObject().get("FileSource").getAsString());
	}

	private void setUploadSignedUrl() throws IOException {
		String response = this.context.getHttpResponse().getBody();
		gsn = new Gson();
		JsonObject root = gsn.fromJson(response, JsonObject.class);
		this.context.setSignedUrl(root.get("Location").getAsJsonObject().get("SignedURL").getAsString());
	}

	private void verifyFailedResponse(String statusCode, String respMsg) throws IOException {

		HttpResponse actualResponse = this.context.getHttpResponse();
		validateResponseCode(statusCode);

		gsnActual = new Gson();

		String expectedRespStr = this.context.getFileUtils().readFromLocalFilePath(respMsg);
		Gson gsnExpected = new Gson();
		JsonObject expectedResponseJO = gsnExpected.fromJson(expectedRespStr, JsonObject.class);
		JsonObject actualResponseJO = gsnActual.fromJson(actualResponse.getBody().toString(), JsonObject.class);

		assertEquals(expectedResponseJO.toString(), actualResponseJO.toString());

	}

}
