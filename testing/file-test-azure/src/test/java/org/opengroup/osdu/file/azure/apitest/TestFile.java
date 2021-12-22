package org.opengroup.osdu.file.azure.apitest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.core.common.model.storage.UpsertRecords;
import org.opengroup.osdu.file.apitest.Config;
import org.opengroup.osdu.file.apitest.File;
import org.opengroup.osdu.file.azure.HttpClientAzure;

import org.opengroup.osdu.file.util.FileUtils;
import org.opengroup.osdu.file.util.LegalTagUtils;
import org.opengroup.osdu.file.util.StorageRecordUtils;
import util.DummyRecordsHelper;
import util.FileUtilsAzure;
import util.StorageUtilAzure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestFile extends File {
  protected static final DummyRecordsHelper RECORDS_HELPER = new DummyRecordsHelper();
  private static String containerName = System.getProperty("STAGING_CONTAINER_NAME", System.getenv("STAGING_CONTAINER_NAME"));

  private static final String storageInstructionsApi = "/files/storageInstructions";
  private static final String retrievalInstructionsApi = "/files/retrievalInstructions";
  private static final String copyDmsApi = "/files/copy";


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
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
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
    String fileID = java.util.UUID.randomUUID().toString();
    ClientResponse response = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(fileID)
    );
    assertNotNull(response);
    assertEquals(HttpStatus.SC_ACCEPTED, response.getStatus());

    // The next request with the same file id should fail
    response = client.send(
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
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
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

  @Test
  public void testFileDmsApis() throws Exception {
    ClientResponse storageInstructionsResponse = client.send(
        storageInstructionsApi,
        "POST",
        getCommonHeader(),
        null);

    assertNotNull(storageInstructionsResponse);
    assertEquals(HttpStatus.SC_OK, storageInstructionsResponse.getStatus());

    // GET Storage Instructions....
    StorageInstructionsResponse storageInstructions = mapper.readValue(
        storageInstructionsResponse.getEntity(String.class), StorageInstructionsResponse.class);

    System.out.println("Storage Response " + storageInstructions);

    // Upload File to Signed URL
    Map<String, String> fileUploadHeaders = new HashMap<>();
    fileUploadHeaders.put("x-ms-blob-type", "BlockBlob");

    assertEquals("AZURE", storageInstructions.getProviderKey());
    assertTrue(storageInstructions.getStorageLocation().containsKey("signedUrl"));
    assertTrue(storageInstructions.getStorageLocation().containsKey("fileSource"));

    ClientResponse fileUploadResponse = client.sendExt(
        storageInstructions.getStorageLocation().get("signedUrl").toString(),
        "PUT", fileUploadHeaders, "foo-bar-content");

    assertEquals(HttpStatus.SC_CREATED, fileUploadResponse.getStatus());

    // Storage metadata create.
    final String legalTagName = LegalTagUtils.createRandomName();
    ClientResponse legalTagCreateResponse = LegalTagUtils.create(client, getCommonHeader(),
        legalTagName);

    assertEquals(HttpStatus.SC_CREATED, legalTagCreateResponse.getStatus());

    final String fileMetadataRecord = StorageRecordUtils.createFileMetadataRecord(
        storageInstructions.getStorageLocation().get("fileSource").toString(), legalTagName);

    ClientResponse storageMetadataUpdateResponse = StorageRecordUtils.sendMetadataRecord(client,
        getCommonHeader(), fileMetadataRecord);

    assertEquals(HttpStatus.SC_CREATED, storageMetadataUpdateResponse.getStatus());

    UpsertRecords createdRecords = mapper.readValue(
        storageMetadataUpdateResponse.getEntity(String.class), UpsertRecords.class);

    // Copy DMS
    ClientResponse copyDmsResponse = client.send(
        copyDmsApi,
        "POST",
        getCommonHeader(),
        StorageRecordUtils.convertStorageMetadatRecordToCopyDmsRequest(fileMetadataRecord));

    assertEquals(HttpStatus.SC_OK, copyDmsResponse.getStatus());

    // Retrieval instructions
    RetrievalInstructionsRequest request = new RetrievalInstructionsRequest();
    request.setDatasetRegistryIds(createdRecords.getRecordIds());

    ClientResponse retrievalInstructionsResponse = client.send(
        retrievalInstructionsApi,
        "POST",
        getCommonHeader(),
        mapper.writeValueAsString(request));

    assertNotNull(retrievalInstructionsResponse);
    assertEquals(HttpStatus.SC_OK, retrievalInstructionsResponse.getStatus());

    RetrievalInstructionsResponse retrievalResponse = mapper.readValue(
        retrievalInstructionsResponse.getEntity(String.class), RetrievalInstructionsResponse.class);

    assertEquals("AZURE", retrievalResponse.getProviderKey());
    assertEquals(1, retrievalResponse.getDatasets().size());

    assertEquals(createdRecords.getRecordIds().get(0),
        retrievalResponse.getDatasets().get(0).getDatasetRegistryId());

    assertTrue(retrievalResponse.getDatasets().get(0).getRetrievalProperties().containsKey("signedUrl"));
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
          if (fileLoc.length >= 2) {
            String fileName = fileLoc[1];
            cloudStorageUtil.deleteCloudFile(containerName, fileName);
          }
        }
      }
    }
  }
}
