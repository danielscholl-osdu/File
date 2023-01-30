/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.gcp.util.url;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class UrlGcsProviderTest {

  private UrlGcsProvider urlGcsProvider = new UrlGcsProvider();
  private static final String BUCKET = "bucket";
  private static final String BUCKET_WITH_SLASH = "/bucket";
  private static final String FILE_PATH = "file";
  private static final String PARTITION_ID = "test-partition";
  public static final String WELL_FORMED_GS_URL = "https://storage.googleapis.com/bucket/";
  public static final String WELL_FORMED_GS_URL_WITH_FILE =
      "https://storage.googleapis.com/bucket/file";

  @Test
  public void getObjectUrlWithoutSlashInBucket() throws MalformedURLException {
    URL objectUrl = urlGcsProvider.getObjectUrl(BUCKET, PARTITION_ID);
    assertEquals(WELL_FORMED_GS_URL, objectUrl.toString());
  }

  @Test
  public void getObjectUrlWithSlashInBucket() throws MalformedURLException {
    URL objectUrl = urlGcsProvider.getObjectUrl(BUCKET_WITH_SLASH, PARTITION_ID);
    assertEquals(WELL_FORMED_GS_URL, objectUrl.toString());
  }

  @Test
  public void testGetObjectUrlWithFile() throws MalformedURLException {
    URL objectUrl = urlGcsProvider.getObjectUrl(BUCKET, FILE_PATH, PARTITION_ID);
    assertEquals(WELL_FORMED_GS_URL_WITH_FILE, objectUrl.toString());
  }
}
