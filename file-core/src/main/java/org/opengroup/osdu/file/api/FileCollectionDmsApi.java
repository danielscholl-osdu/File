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
import io.swagger.v3.oas.annotations.Parameter;
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
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
@Hidden
@RequestMapping(value = "/v2/file-collections")
@Tag(name = "file-collection-dms-api", description = "File Collection Dms API")
public class FileCollectionDmsApi {

  @Autowired
  @Qualifier("FileCollectionDmsService")
  IDmsService fileCollectionDmsService;

  @Operation(summary = "${fileCollectionDmsApi.getStorageInstructions.summary}", description = "${fileCollectionDmsApi.getStorageInstructions.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-collection-dms-api" })
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
  public ResponseEntity<StorageInstructionsResponse> getStorageInstructions(@Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
      example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) {
    StorageInstructionsResponse storageInstructionsResp = fileCollectionDmsService.getStorageInstructions(expiryTime);
    return new ResponseEntity<>(storageInstructionsResp, HttpStatus.OK);
  }

  @Operation(summary = "${fileCollectionDmsApi.getRetrievalInstructions.summary}", description = "${fileCollectionDmsApi.getRetrievalInstructions.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-collection-dms-api" })
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
      @RequestBody RetrievalInstructionsRequest retrievalInstructionsRequest, @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
      example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) {
    RetrievalInstructionsResponse retrievalInstructionsResp = fileCollectionDmsService.getRetrievalInstructions(retrievalInstructionsRequest, expiryTime);
    return new ResponseEntity<>(retrievalInstructionsResp, HttpStatus.OK);
  }

  @Operation(summary = "${fileCollectionDmsApi.copyDms.summary}", description = "${fileCollectionDmsApi.copyDms.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-collection-dms-api" })
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
    List<CopyDmsResponse> copyOpResponse = fileCollectionDmsService.copyDatasetsToPersistentLocation(copyDmsRequest.getDatasetSources());
    return new ResponseEntity<>(copyOpResponse, HttpStatus.OK);
  }
}
