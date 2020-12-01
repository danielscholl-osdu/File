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

package org.opengroup.osdu.file.provider.azure.service;

import com.azure.cosmos.implementation.InternalServerErrorException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ServiceHelper {

  // refer https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/resource-name-rules#microsoftstorage for naming convention
  private final String absoluteFilePathPattern = "https://[a-z0-9][a-z0-9^-]*.blob.core.windows.net/(?<containerName>[^/]*)/(?<filePath>.*)";

  public String getContainerNameFromAbsoluteFilePath(String absoluteFilePath) {
    Pattern pattern = Pattern.compile(absoluteFilePathPattern);
    Matcher matcher = pattern.matcher(absoluteFilePath);
    try {
      matcher.matches();
      return matcher.group("containerName");
    }
    catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("Could not parse container name from file path provided {%s}\n %s", absoluteFilePath, e));
    }
  }

  public String getRelativeFilePathFromAbsoluteFilePath(String absoluteFilePath) {
    Pattern pattern = Pattern.compile(absoluteFilePathPattern);
    Matcher matcher = pattern.matcher(absoluteFilePath);
    try {
      matcher.matches();
      return matcher.group("filePath");
    }
    catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("Could not parse relative file path from file path provided {%s}\n %s", absoluteFilePath, e));
    }
  }

}
