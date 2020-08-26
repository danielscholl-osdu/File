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

package org.opengroup.osdu.file.gcp.apitest;

import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.apitest.File;
import org.opengroup.osdu.file.gcp.HttpClientGCP;
import org.opengroup.osdu.file.gcp.util.GcpConfig;
import org.opengroup.osdu.file.gcp.util.StorageUtilGCP;
import org.opengroup.osdu.file.util.FileUtils;

public class TestFile extends File {

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientGCP();
    cloudStorageUtil = new StorageUtilGCP();
  }

  //We can't fully control filename generation in integration tests, to clear storage
  // extra requests to file service required
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

        String filename = fileLocationResponse.getLocation()
            .replaceFirst("gs://" + GcpConfig.getFileBucketName() + "/", "");

        cloudStorageUtil.deleteCloudFile(GcpConfig.getFileBucketName(), filename);
      }
    }
  }

}
