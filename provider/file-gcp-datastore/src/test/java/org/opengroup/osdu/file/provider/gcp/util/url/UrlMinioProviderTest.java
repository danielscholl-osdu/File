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
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;

@ExtendWith(MockitoExtension.class)
public class UrlMinioProviderTest {

  private static final String MINIO_URL = "https://minio.com";
  private static final String MINIO_URL_WITH_SLASH = "https://minio.com/";
  private static final String BUCKET = "bucket";
  private static final String BUCKET_WITH_SLASH = "/bucket";
  private static final String FILE_PATH = "file";
  private static final String PARTITION_ID = "test-partition";
  public static final String WELL_FORMED_MINIO_URL = "https://minio.com/bucket/";
  public static final String WELL_FORMED_MINIO_URL_WITH_FILE = "https://minio.com/bucket/file";

  @Mock
  private EnvironmentResolver environmentResolver;

  private UrlMinioProvider urlMinioProvider;

  @Test
  public void getObjectUrlWithoutSlashInUrlAndBucket() throws MalformedURLException {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_URL);
    urlMinioProvider = new UrlMinioProvider(environmentResolver);
    URL objectUrl = urlMinioProvider.getObjectUrl(BUCKET, PARTITION_ID);
    assertEquals(WELL_FORMED_MINIO_URL, objectUrl.toString());
  }

  @Test
  public void getObjectUrlWithSlashInUrlAndBucket() throws MalformedURLException {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_URL_WITH_SLASH);
    urlMinioProvider = new UrlMinioProvider(environmentResolver);
    URL objectUrl = urlMinioProvider.getObjectUrl(BUCKET_WITH_SLASH, PARTITION_ID);
    assertEquals(WELL_FORMED_MINIO_URL, objectUrl.toString());
  }

  @Test
  public void getObjectUrlWithSlashInUrlAndWithoutInBucket() throws MalformedURLException {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_URL_WITH_SLASH);
    urlMinioProvider = new UrlMinioProvider(environmentResolver);
    URL objectUrl = urlMinioProvider.getObjectUrl(BUCKET, PARTITION_ID);
    assertEquals(WELL_FORMED_MINIO_URL, objectUrl.toString());
  }

  @Test
  public void getObjectUrlWithoutSlashInUrlAndWithInBucket() throws MalformedURLException {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_URL);
    urlMinioProvider = new UrlMinioProvider(environmentResolver);
    URL objectUrl = urlMinioProvider.getObjectUrl(BUCKET_WITH_SLASH, PARTITION_ID);
    assertEquals(WELL_FORMED_MINIO_URL, objectUrl.toString());
  }

  @Test
  public void testGetObjectUrlWithFile() throws MalformedURLException {
    when(environmentResolver.getTransferProtocol(PARTITION_ID)).thenReturn(MINIO_URL);
    urlMinioProvider = new UrlMinioProvider(environmentResolver);
    URL objectUrl = urlMinioProvider.getObjectUrl(BUCKET, FILE_PATH, PARTITION_ID);
    assertEquals(WELL_FORMED_MINIO_URL_WITH_FILE, objectUrl.toString());
  }
}
