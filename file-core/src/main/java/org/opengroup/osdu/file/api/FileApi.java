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
import org.opengroup.osdu.core.common.model.file.FileRequest;
import org.opengroup.osdu.core.common.model.file.FileResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.provider.interfaces.IFileService;
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
@Hidden
@Tag(name = "file-api", description = "File API")
public class FileApi {

  final DpsHeaders headers;
  final IFileService fileService;

  // TODO: Create the permission for os-file and change pre authorize annotation
  @Operation(summary = "${fileApi.getFile.summary}", description = "${fileApi.getFile.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = FileResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/v2/getFile")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
  public FileResponse getFile(@RequestBody FileRequest request) {
    log.debug("Get file request received : {}", request);
    FileResponse fileResponse = fileService.getFile(request, headers);
    log.debug("Get file result ready : {}", fileResponse);
    return fileResponse;
  }
}
