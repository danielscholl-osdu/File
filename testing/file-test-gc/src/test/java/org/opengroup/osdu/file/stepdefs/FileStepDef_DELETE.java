package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
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

public class FileStepDef_DELETE implements En {

  @Inject
  private FileScope context;

  static String[] GetListBaseFilterArray;
  static String[] GetListVersionFilterArray;
  String queryParameter;
  Gson gsn = null;

  private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  List<HashMap<String, String>> list_fileDMSParameterMap = new ArrayList<HashMap<String, String>>();

  public FileStepDef_DELETE() {
	  
	  Given("I hit File service Delete metadata endpoint with a valid Id", () -> {
		  String id = this.context.getId();
		  LOGGER.info("Id to be deleted : " + id);
	      HttpRequest httpRequest = HttpRequest.builder()
	          .url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + id
		              + TestConstants.GET_METADATA_ENDPOINT2).httpMethod(HttpRequest.DELETE)
	          .requestHeaders(this.context.getAuthHeaders()).build();
	      LOGGER.info("Delete httpRequest : - " + httpRequest);
	      HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
	      this.context.setHttpResponse(response);
	      LOGGER.log(Level.INFO, "Delete resp - " + response.toString());
	      assertEquals("204", String.valueOf(response.getCode()));
	      
	    });
	  
	  Given("I hit File service Delete metadata endpoint with a invalid Id", () -> {
		  String id = this.context.getId();
	      HttpRequest httpRequest = HttpRequest.builder()
	          .url(TestConstants.HOST + TestConstants.GET_SIGNEDURL_DOWNLOAD_ENDPOINT1 + id
		              + TestConstants.GET_METADATA_ENDPOINT2).httpMethod(HttpRequest.DELETE)
	          .requestHeaders(this.context.getAuthHeaders()).build();

	      HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
	      this.context.setHttpResponse(response);
	      LOGGER.log(Level.INFO, "resp - " + response.toString());
	      assertEquals("404", String.valueOf(response.getCode()));
	      
	    });
	  
	  
	    Then("Delete service should respond back with {string}", (String reponseStatusCode) -> {
	        HttpResponse response = this.context.getHttpResponse();
	        if (response != null) {
	          assertEquals(reponseStatusCode, String.valueOf(response.getCode()));
	        }
	      });
  }
}
