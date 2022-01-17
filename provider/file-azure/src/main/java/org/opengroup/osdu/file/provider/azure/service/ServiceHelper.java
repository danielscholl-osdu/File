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

  private static final String CONTAINER_NAME = "containerName";
  private static final String FILE_SYSTEM_NAME = "fileSystemName";
  private static final String FILE_PATH = "filePath";
  private static final String DIRECTORY_PATH = "directoryPath";

  // refer https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/resource-name-rules#microsoftstorage for naming convention
  private final String absoluteFilePathPattern = "https://[a-z0-9][a-z0-9^-]*.blob.core.windows.net/(?<containerName>[^/]*)/(?<filePath>.*)";
  private final String absoluteDirectoryPathPattern = "https://[a-z0-9][a-z0-9^-]*..dfs.core.windows.net/(?<fileSystemName>[^/]*)/(?<directoryPath>.*)";

  public String getContainerNameFromAbsoluteFilePath(String absoluteFilePath) {
    return match(absoluteFilePathPattern, absoluteFilePath, CONTAINER_NAME);
  }

  public String getRelativeFilePathFromAbsoluteFilePath(String absoluteFilePath) {
    return match(absoluteFilePathPattern, absoluteFilePath, FILE_PATH);
  }

  public String getFileSystemNameFromAbsoluteDirectoryPath(String absoluteFilePath) {
    return match(absoluteDirectoryPathPattern, absoluteFilePath, FILE_SYSTEM_NAME);
  }
  public String getRelativeDirectoryPathFromAbsoluteDirectoryPath(String absoluteFilePath) {
    return match(absoluteDirectoryPathPattern, absoluteFilePath, DIRECTORY_PATH);
  }

  private String match(String absoluteFilePathPattern, String input,String matchingString) {
    Pattern pattern = Pattern.compile(absoluteFilePathPattern);
    Matcher matcher = pattern.matcher(input);
    try {
      matcher.matches();
      return matcher.group(matchingString);
    }
    catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("Could not parse {%s} from file path provided {%s}\n %s",matchingString, input, e));
    }
  }
}
