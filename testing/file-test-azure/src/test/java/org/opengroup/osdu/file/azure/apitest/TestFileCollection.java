package org.opengroup.osdu.file.azure.apitest;

import org.opengroup.osdu.file.azure.Helper.DataLakeHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.storage.UpsertRecords;
import org.opengroup.osdu.file.TestBase;
import org.opengroup.osdu.file.azure.HttpClientAzure;
import org.opengroup.osdu.file.util.LegalTagUtils;
import org.opengroup.osdu.file.util.StorageRecordUtils;
import org.opengroup.osdu.file.util.TestUtils;
import util.StorageUtilAzure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFileCollection extends TestBase {

  private static final String storageInstructionsApi = "/file-collections/storageInstructions";
  private static final String retrievalInstructionsApi = "/file-collections/retrievalInstructions";
  private static final String copyDmsApi = "/file-collections/copy";
  private static ObjectMapper mapper = new ObjectMapper();
  private DataLakeHelper dataLakeHelper = new DataLakeHelper();
  private static String FIRST_FILE_NAME = "upload_first.txt";
  private static String SECOND_FILE_NAME = "upload_second.txt";
  private static String THIRD_FILE_NAME = "upload_third.txt";
  private static final String FIRST_FILE_CONTENT = "foo-bar1";
  private static final String SECOND_FILE_CONTENT = "foo-bar2";
  private static final String THIRD_FILE_CONTENT = "foo-bar3";

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientAzure();
    cloudStorageUtil = new StorageUtilAzure();
  }

  @Test
  public void testFileCollectionDmsApis() throws Exception {
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

    assertEquals("AZURE", storageInstructions.getProviderKey());
    assertTrue(storageInstructions.getStorageLocation().containsKey("signedUrl"));
    assertTrue(storageInstructions.getStorageLocation().containsKey("fileCollectionSource"));

    dataLakeHelper.uploadFile(storageInstructions.getStorageLocation().get("signedUrl").toString(),
        FIRST_FILE_CONTENT, FIRST_FILE_NAME);
    dataLakeHelper.uploadFile(storageInstructions.getStorageLocation().get("signedUrl").toString(),
        SECOND_FILE_CONTENT, SECOND_FILE_NAME);
    dataLakeHelper.uploadFile(storageInstructions.getStorageLocation().get("signedUrl").toString(),
        THIRD_FILE_CONTENT, THIRD_FILE_NAME);

    // Storage metadata create.
   final String legalTagName = LegalTagUtils.createRandomName();
    ClientResponse legalTagCreateResponse = LegalTagUtils.create(client, getCommonHeader(),
        legalTagName);

    assertEquals(HttpStatus.SC_CREATED, legalTagCreateResponse.getStatus());

    final String fileMetadataRecord = StorageRecordUtils.prepareFileMetadataRecord(
        storageInstructions.getStorageLocation().get("fileCollectionSource").toString(), legalTagName
        , TestUtils.FILE_COLLECTION_KIND);

    ClientResponse storageMetadataUpdateResponse = StorageRecordUtils.createMetadataRecord(client,
        getCommonHeader(), fileMetadataRecord);

    assertEquals(HttpStatus.SC_CREATED, storageMetadataUpdateResponse.getStatus());

    UpsertRecords createdRecords = mapper.readValue(
        storageMetadataUpdateResponse.getEntity(String.class), UpsertRecords.class);

    // Copy DMS
    ClientResponse copyDmsResponse = client.send(
        copyDmsApi,
        "POST",
        getCommonHeader(),
        StorageRecordUtils.convertStorageMetadataRecordToCopyDmsRequest(fileMetadataRecord));

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

    assertEquals("AZURE", retrievalResponse.getDatasets().get(0).getProviderKey());
    assertEquals(1, retrievalResponse.getDatasets().size());

    assertEquals(createdRecords.getRecordIds().get(0),
        retrievalResponse.getDatasets().get(0).getDatasetRegistryId());

    assertTrue(retrievalResponse.getDatasets().get(0).getRetrievalProperties().containsKey("signedUrl"));

    //Download file
    ByteArrayOutputStream firstDownloadedContent = dataLakeHelper.downloadFile(retrievalResponse.getDatasets().get(0)
        .getRetrievalProperties().get("signedUrl").toString(), FIRST_FILE_NAME);
    ByteArrayOutputStream secondDownloadedContent = dataLakeHelper.downloadFile(retrievalResponse.getDatasets().get(0)
        .getRetrievalProperties().get("signedUrl").toString(), SECOND_FILE_NAME);
    ByteArrayOutputStream thirdDownloadedContent = dataLakeHelper.downloadFile(retrievalResponse.getDatasets().get(0)
        .getRetrievalProperties().get("signedUrl").toString(), THIRD_FILE_NAME);

    assertEquals(FIRST_FILE_CONTENT, firstDownloadedContent.toString());
    assertEquals(SECOND_FILE_CONTENT, secondDownloadedContent.toString());
    assertEquals(THIRD_FILE_CONTENT, thirdDownloadedContent.toString());
  }
}
