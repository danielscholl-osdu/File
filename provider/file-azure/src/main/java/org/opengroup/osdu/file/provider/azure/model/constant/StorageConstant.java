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

package org.opengroup.osdu.file.provider.azure.model.constant;

public final class StorageConstant {

  public static final String AZURE_PROTOCOL = "https://";
  public static final int AZURE_MAX_FILEPATH = 1024;
  public static final String BLOB_STORAGE_ACCOUNT_BASE_URI_REGEX = "%s%s.blob.core.windows.net";
  public static final String BLOB_RESOURCE_BASE_URI_REGEX = "%s%s.blob.core.windows.net/%s/%s";
  public static final String SLASH = "/";
  private StorageConstant() {
  }

}
