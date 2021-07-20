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

package org.opengroup.osdu.file.ibm.apitest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.apitest.Config;
import org.opengroup.osdu.file.apitest.File;
import org.opengroup.osdu.file.ibm.util.HttpClientIBM;
import org.opengroup.osdu.file.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.apache.http.HttpStatus;
import com.sun.jersey.api.client.ClientResponse;

public class TestFile extends File {

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientIBM();
  }

  
  @Test
  @Override
  @Disabled
  public void first_getLocation_then_shouldReturnFileList_sameFileId() throws Exception {
	    
	  }
  
  //We can't fully control filename generation in integration tests, to clear storage
  // extra requests to file service required
  @AfterAll
  public static void tearDown() throws Exception {
//	  TODO:delete records created in couch as part of test case execution
//    if (!locationResponses.isEmpty()) {
//      for (LocationResponse response : locationResponses) {
//      }
//    }
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

}
