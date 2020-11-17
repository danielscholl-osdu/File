/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.provider.interfaces.IFileListService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
public class FileListApi {

  final DpsHeaders headers;
  final IFileListService fileListService;

  // TODO: Create the permission for os-file and change pre authorize annotation
  @PostMapping("/getFileList")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
  public FileListResponse getFileList(@RequestBody FileListRequest request) {
    log.debug("File list request received : {}", request);
    FileListResponse fileListResponse = fileListService.getFileList(request, headers);
    log.debug("File list result ready : {}", fileListResponse);
    return fileListResponse;
  }

}
