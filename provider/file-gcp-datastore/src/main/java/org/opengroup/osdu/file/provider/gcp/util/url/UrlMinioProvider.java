/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp.util.url;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.file.provider.gcp.config.obm.EnvironmentResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "obmDriver", havingValue = "minio")
public class UrlMinioProvider implements UrlProvider {

  private final EnvironmentResolver environmentResolver;

  @Override
  public URL getObjectUrl(String bucketName, String partitionId) throws MalformedURLException {
    return getObjectUrl(bucketName, "", partitionId);
  }

  @Override
  public URL getObjectUrl(String bucketName, String filepath, String partitionId)
      throws MalformedURLException {

    URI normalizedURI = URI.create(
            String.format(
                RESOURCE_ACCESS_STRING_FORMAT,
                environmentResolver.getTransferProtocol(partitionId),
                bucketName,
                filepath))
        .normalize();

    return normalizedURI.toURL();

  }
}
