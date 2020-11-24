/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.TestBase;
import org.opengroup.osdu.file.util.FileUtils;


public abstract class File extends TestBase {

  protected static final String getLocation = "/getLocation";
  protected static final String getFileLocation = "/getFileLocation";
  protected static final String getFileList = "/getFileList";

  protected static List<LocationResponse> locationResponses = new ArrayList<>();
  protected static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void shouldReturnNotEmptyFileIdAndSignedURL_whenGetLocationWithoutBody()
      throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        "{}");

    LocationResponse locationResponse = mapper
        .readValue(getLocationResponse.getEntity(String.class), LocationResponse.class);

    assertFalse(locationResponse.getFileID().isEmpty() && locationResponse.getLocation().isEmpty());
    locationResponses.add(locationResponse);
  }

  @Test
  public void shouldReturnSignedUrlAndSameFileId_whenGetLocationWithBody() throws Exception {
    String fileID = FileUtils.generateUniqueFileID();
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(fileID)
    );

    LocationResponse locationResponse = mapper
        .readValue(getLocationResponse.getEntity(String.class), LocationResponse.class);

    assertFalse(locationResponse.getLocation().isEmpty());
    assertEquals(fileID, locationResponse.getFileID());
    locationResponses.add(locationResponse);
  }

  @Test
  public void first_getLocation_then_GetFileLocationShouldReturnUnsignedUrl() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(FileUtils.generateUniqueFileID())
    );

    LocationResponse locationResponse = mapper
        .readValue(getLocationResponse.getEntity(String.class), LocationResponse.class);

    ClientResponse getFileLocationResponse = client.send(
        getFileLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(locationResponse.getFileID()));

    FileLocationResponse fileLocationResponse = mapper
        .readValue(getFileLocationResponse.getEntity(String.class), FileLocationResponse.class);

    assertNotEquals(HttpStatus.SC_BAD_REQUEST, getFileLocationResponse.getStatus());
    assertFalse(fileLocationResponse.getLocation().isEmpty());
    locationResponses.add(locationResponse);
  }

  @Test
  public void shouldReturnBadRequest_whenGetNonExistingFileLocation() throws Exception {
    ClientResponse getFileLocationResponse = client.send(
        getFileLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody(FileUtils.generateUniqueFileID()));

    assertEquals(HttpStatus.SC_BAD_REQUEST, getFileLocationResponse.getStatus());
  }

  @Test
  public void shouldReturnBadRequest_whenGivenNotValidFileId() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileRequestBody("/" + FileUtils.generateUniqueFileID() + "/")
    );
    assertEquals(HttpStatus.SC_BAD_REQUEST, getLocationResponse.getStatus());
  }

  @Test
  public void first_getLocation_then_shouldReturnFileList_sameFileId() throws Exception {
    LocalDateTime from = LocalDateTime.now(ZoneId.of(Config.getTimeZone()));

    // sleeping because server clock can get out of sync by a fraction of a second
    // which can cause this test to fail
    Thread.sleep(5000);

    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getCommonHeader(),
        "{}");

    LocationResponse locationResponse = mapper
        .readValue(getLocationResponse.getEntity(String.class), LocationResponse.class);

    Thread.sleep(5000);
    LocalDateTime to = LocalDateTime.now(ZoneId.of(Config.getTimeZone()));

    String fileListRequestBody = FileUtils.generateFileListRequestBody(from, to, 0, (short) 1);
    ClientResponse fileListResponse = client.send(
        getFileList,
        "POST",
        getCommonHeader(),
        fileListRequestBody
    );
    try {
      FileListResponse fileList = mapper
          .readValue(fileListResponse.getEntity(String.class), FileListResponse.class);

      assertFalse(fileList.getContent().isEmpty());
      FileLocation fileLocation = fileList.getContent().get(0);
      assertEquals(fileLocation.getFileID(), locationResponse.getFileID());
    } catch (Throwable e) {
      locationResponses.add(locationResponse);
      throw new Exception(String
          .format("%s request failed, with body %s , make sure that TIME_ZONE = %s is correct",
              getFileList, fileListRequestBody, Config.getTimeZone()), e);
    }
    locationResponses.add(locationResponse);
  }

  @Test
  public void shouldReturnBadRequest_whenGetFileListInvalidRequest() throws Exception {
    LocalDateTime now = LocalDateTime.now(ZoneId.of(Config.getTimeZone()));
    ClientResponse fileListResponse = client.send(
        getFileList,
        "POST",
        getCommonHeader(),
        FileUtils.generateFileListRequestBody(now.plusHours(1), now, 0, (short) -1)
    );
    assertEquals(HttpStatus.SC_BAD_REQUEST, fileListResponse.getStatus());
  }

  @Test
  public void shouldReturnUnauthorized_whenGivenInvalidPartitionId() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders("invalid_partition", client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
  }

  @Test
  public void shouldReturnUnauthorized_whenPartitionIdNotGiven() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(null, client.getAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
  }

  @Test
  public void shouldReturnUnauthorized_whenGivenAnonimus() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(Config.getDataPartitionId(), null),
        "{}");
    assertEquals(HttpStatus.SC_UNAUTHORIZED, getLocationResponse.getStatus());
  }

  @Test
  @Disabled
  public void getLocationShouldReturnForbidden_whenGivenNoDataAccess() throws Exception {
    ClientResponse getLocationResponse = client.send(
        getLocation,
        "POST",
        getHeaders(Config.getDataPartitionId(), client.getNoDataAccessToken()),
        "{}");
    assertEquals(HttpStatus.SC_FORBIDDEN, getLocationResponse.getStatus());
  }

}
