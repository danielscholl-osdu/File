/*
 * Copyright 2020  Microsoft Corporation
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

package org.opengroup.osdu.file.provider.azure;

import static java.lang.String.format;
import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.AZURE_PROTOCOL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.assertj.core.api.Condition;

public final class TestUtils {

  public static final String AUTHORIZATION_TOKEN = "authToken";
  public static final String PARTITION = "partition";
  public static final String USER_DES_ID = "common-user";
  public static final String CONTAINER_NAME = "odes-os-file-temp";
  public static final String STORAGE_NAME = "adotestfqofqosn0o4sa";

  public static final String UUID_REGEX = "(.{8})(.{4})(.{4})(.{4})(.{12})";
  public static final Pattern AZURE_OBJECT_URI
      = Pattern.compile("^https://[\\w,\\s-]+/[\\w,\\s-]+/[\\w,\\s-]+");
  public static final Condition<String> UUID_CONDITION
          = new Condition<>(TestUtils::isValidUuid, "Valid UUID");
  public static final Condition<String> AZURE_URL_CONDITION
              = new Condition<>(TestUtils::isValidSignedUrl, "Signed URL for AZURE object");
  public static final String FILE_ID = "test-file-id.tmp";

  private TestUtils() {
  }

  private static boolean isValidUuid(String uuid) {
    try {
      String normalizedUuid = uuid.replaceAll(UUID_REGEX, "$1-$2-$3-$4-$5");
      UUID.fromString(normalizedUuid);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static boolean isValidSignedUrl(String url) {
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  public static URI getAzureObjectUri(String containerName, String folderName, String filename) {
    return URI.create(format("%s%s.blob.core.windows.net/%s/%s", AZURE_PROTOCOL, STORAGE_NAME, containerName, filename));
  }

  @SneakyThrows
  public static URL getAzureObjectUrl(String containerName, String folderName, String filename) {
    return new URL(format(
        "%s%s.blob.core.windows.net/%s/%s?sv=2019-07-07&se=2020-08-08T13A36A49Z&skoid=0fa47244-83d8-4311-b05c-fefb49d8b0a9&sktid=58975fd3-4977-44d0-bea8-37af0baac100&skt=2020-08-08T013A3649Z&ske=2020-08-08T133649Z&sks=b&skv=2019-07-07&sr=b&sp=r&sig=Hh5xGUpvTkEDeArXaWmV6FnSOMbYLRdHSfGlOlsC7wD2020-08-07",
        AZURE_PROTOCOL, STORAGE_NAME, containerName, filename));
  }

  public static Instant now() {
    return Instant.now(Clock.systemUTC());
  }

  public static String getUuidString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
