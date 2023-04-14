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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.logging.AuditLogger;
import org.opengroup.osdu.file.provider.interfaces.IFileListService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
@Hidden
@Tag(name = "file-list-api", description = "File List API")
public class FileListApi {

  final DpsHeaders headers;
  final IFileListService fileListService;
  private final AuditLogger auditLogger;

  // TODO: Create the permission for os-file and change pre authorize annotation
  @Operation(summary = "${fileListApi.getFileList.summary}", description = "${fileListApi.getFileList.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-list-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File list page", content = { @Content(schema = @Schema(implementation = FileListResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/v2/getFileList")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
  public FileListResponse getFileList(@RequestBody FileListRequest request) {
    log.debug("File list request received : {}", request);
    FileListResponse fileListResponse = fileListService.getFileList(request, headers);
    log.debug("File list result ready : {}", fileListResponse);
    this.auditLogger.readFileListSuccess(Collections.singletonList(fileListResponse.toString()));
    return fileListResponse;
  }

}
