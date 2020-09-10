package org.opengroup.osdu.file.azure.apitest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.apitest.Config;
import org.opengroup.osdu.file.apitest.File;
import org.opengroup.osdu.file.azure.HttpClientAzure;

import org.opengroup.osdu.file.util.FileUtils;
import util.DummyRecordsHelper;
import util.FileUtilsAzure;
import util.StorageUtilAzure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFile extends File {
  protected static final DummyRecordsHelper RECORDS_HELPER = new DummyRecordsHelper();
  private static String containerName = System.getProperty("DATA_PARTITION_ID", System.getenv("DATA_PARTITION_ID"));
  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientAzure();
    cloudStorageUtil = new StorageUtilAzure();
  }


  @Test
  @Override
  public void getLocationShouldReturnForbidden_whenGivenNoDataAccess() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(Config.getDataPartitionId(), client.getNoDataAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
  }

  @Test
  @Override
  public void shouldReturnUnauthorized_whenGivenAnonimus() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(Config.getDataPartitionId(), null),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }
  @Test
  @Override
  public void shouldReturnUnauthorized_whenPartitionIdNotGiven() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(null, client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

  @Test
  @Override
  public void shouldReturnUnauthorized_whenGivenInvalidPartitionId() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders("invalid_partition", client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

  @Test
  public void getLocationShouldReturnBadrequest_whenGivenAlreadyExistsFileID() throws Exception {
    String fileID = System.getProperty("EXIST_FILE_ID", System.getenv("EXIST_FILE_ID"));
    ClientResponse response = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(fileID)
    );
    assertNotNull(response);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());


  }

  @Test
  public void getLocationShouldReturnBadrequest_whenFileIDLengthExceeds() throws Exception {
    String fileID = FileUtilsAzure.generateUniqueFileID();
    ClientResponse response = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(fileID)
    );
    assertNotNull(response);
    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
    DummyRecordsHelper.BadRequestMock responseObject = RECORDS_HELPER.getRecordsMockFromBadRequestResponse(response);
    String resp = "The maximum filepath length is 1024 characters";
    assertThat(responseObject.message, containsString(resp));
  }

  @Test
  public void getFileLocationShouldReturnBadrequest_whenRequestBodyEmpty() throws Exception {
    ClientResponse getFileLocationResponse = client.send(
        getFileLocation,
        "POST",
        getCommonHeader(),
        "{}");
    assertNotNull(getFileLocationResponse);
    assertEquals(HttpStatus.SC_BAD_REQUEST, getFileLocationResponse.getStatus());
    DummyRecordsHelper.BadRequestMock responseObject = RECORDS_HELPER.getRecordsMockFromBadRequestResponse(getFileLocationResponse);
    String resp = "ConstraintViolationException: Invalid FileLocationRequest";
    assertThat(responseObject.message, containsString(resp));
  }

  @Test
  public void getFileLocationShouldReturnBadrequest_whenGivenNotValidFileId() throws Exception {
    ClientResponse getFileLocationResponse = client.send(
        getFileLocation,
        "POST",
        getCommonHeader(),
        DummyRecordsHelper.buildInvalidFileLocationPayload("test"));
    assertNotNull(getFileLocationResponse);
    assertEquals(HttpStatus.SC_BAD_REQUEST, getFileLocationResponse.getStatus());
    DummyRecordsHelper.BadRequestMock responseObject = RECORDS_HELPER.getRecordsMockFromBadRequestResponse(getFileLocationResponse);
    String resp = "ConstraintViolationException: Invalid FileLocationRequest";
    assertThat(responseObject.message, containsString(resp));
  }

  @Test
  public void getFileListShouldReturnForbidden_whenGivenNoDataAccess() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getFileList,
        "POST",
        getHeaders(Config.getDataPartitionId(), client.getNoDataAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
  }

  @Test
  public void getFileListShouldReturnUnauthorized_whenGivenAnonimus() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getFileList,
        "POST",
        getHeaders(Config.getDataPartitionId(), null),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

  @Test
  public void getFileListShouldReturnUnauthorized_whenPartitionIdNotGiven() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getFileList,
        "POST",
        getHeaders(null, client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

  @Test
  public void getFileListShouldReturnUnauthorized_whenGivenInvalidPartitionId() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getFileList,
        "POST",
        getHeaders("invalid_partition", client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

  @Test
  public void getFileList_shouldReturnBadrequest_whenrecordnotFound() throws Exception {
    LocalDateTime from = LocalDateTime.now(ZoneId.of(Config.getTimeZone()));
    LocalDateTime to = LocalDateTime.now(ZoneId.of(Config.getTimeZone()));

    String fileListRequestBody = FileUtilsAzure.generateFileListRequestBody(0, from, to, (short) 1);

    ClientResponse fileListResponse = client.send(
        getFileList,
        "POST",
        getCommonHeader(),
        fileListRequestBody
    );
    assertNotNull(fileListResponse);
    assertEquals(HttpStatus.SC_BAD_REQUEST, fileListResponse.getStatus());
  }

  @Test
  public void getFileListShouldReturnBadrequest_whenRequestBodyEmpty() throws Exception {
    ClientResponse getFileLocationResponse = client.send(
        getFileList,
        "POST",
        getCommonHeader(),
        "{}");
    assertNotNull(getFileLocationResponse);
    assertEquals(HttpStatus.SC_BAD_REQUEST, getFileLocationResponse.getStatus());
    DummyRecordsHelper.BadRequestMock responseObject = RECORDS_HELPER.getRecordsMockFromBadRequestResponse(getFileLocationResponse);
    String resp = "ConstraintViolationException: Invalid FileListRequest";
    assertThat(responseObject.message, containsString(resp));
  }


  @AfterAll
  public static void tearDown() throws Exception {
    if (!locationResponses.isEmpty()) {
      for (LocationResponse response : locationResponses) {
        ClientResponse getFileLocationResponse = client.send(
            getFileLocation,
            "POST",
            getCommonHeader(),
            FileUtils.generateFileRequestBody(response.getFileID()));

        FileLocationResponse fileLocationResponse = mapper
            .readValue(getFileLocationResponse.getEntity(String.class), FileLocationResponse.class);
        if(fileLocationResponse!=null && StringUtils.isNotBlank(fileLocationResponse.getLocation())) {
          String fileLoc[] = fileLocationResponse.getLocation().split(containerName);
          String fileName = fileLoc[1];
          cloudStorageUtil.deleteCloudFile(containerName, fileName);

        }
      }
    }
  }
}

