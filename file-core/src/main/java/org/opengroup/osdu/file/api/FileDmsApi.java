/*
 * Copyright 2021 Microsoft Corporation
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.constants.DatasetConstants;
import org.opengroup.osdu.core.common.dms.model.CopyDmsRequest;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
@RequestMapping(value = "/v2/files")
@Hidden
@Tag(name = "file-dms-api", description = "File Dms API")
public class FileDmsApi {

  final DpsHeaders headers;

  @Autowired
  @Qualifier("FileDmsService")
  private IDmsService fileDmsService;

  @Operation(summary = "${fileDmsApi.getStorageInstructions.summary}", description = "${fileDmsApi.getStorageInstructions.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-dms-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = StorageInstructionsResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/storageInstructions")
  @PreAuthorize("@authorizationFilter.hasPermission('" + DatasetConstants.DATASET_EDITOR_ROLE + "')")
  public ResponseEntity<StorageInstructionsResponse> getStorageInstructions() {
    StorageInstructionsResponse storageInstructionsResp = fileDmsService.getStorageInstructions();
    return new ResponseEntity<>(storageInstructionsResp, HttpStatus.OK);
  }

  @Operation(summary = "${fileDmsApi.getRetrievalInstructions.summary}", description = "${fileDmsApi.getRetrievalInstructions.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-dms-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = RetrievalInstructionsResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/retrievalInstructions")
  @PreAuthorize("@authorizationFilter.hasPermission('" + DatasetConstants.DATASET_VIEWER_ROLE + "')")
  public ResponseEntity<RetrievalInstructionsResponse> getRetrievalInstructions(
      @RequestBody RetrievalInstructionsRequest retrievalInstructionsRequest) {
    RetrievalInstructionsResponse retrievalInstructionsResp = fileDmsService.getRetrievalInstructions(retrievalInstructionsRequest);
    return new ResponseEntity<>(retrievalInstructionsResp, HttpStatus.OK);
  }

  @Operation(summary = "${fileDmsApi.copyDms.summary}", description = "${fileDmsApi.copyDms.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-dms-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK", content = { @Content(array = @ArraySchema(schema = @Schema(implementation = CopyDmsResponse.class)))}),
      @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/copy")
  @PreAuthorize("@authorizationFilter.hasPermission('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "')")
  public ResponseEntity<List<CopyDmsResponse>> copyDms(@RequestBody CopyDmsRequest copyDmsRequest) {
    List<CopyDmsResponse> copyOpResponse = fileDmsService.copyDatasetsToPersistentLocation(copyDmsRequest.getDatasetSources());
    return new ResponseEntity<>(copyOpResponse, HttpStatus.OK);
  }
}
