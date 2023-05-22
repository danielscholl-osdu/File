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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.logging.AuditLogger;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.interfaces.ILocationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
@Tag(name = "file-location-api", description = "File Location API")
public class FileLocationApi {

  final DpsHeaders headers;
  final ILocationService locationService;
  private final AuditLogger auditLogger;

  // TODO: Create the permission for os-file and change pre authorize annotation
  @Hidden
  @Operation(summary = "${fileLocationApi.getLocation.summary}", description = "${fileLocationApi.getLocation.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-location-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Created location", content = { @Content(schema = @Schema(implementation = LocationResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/v2/getLocation")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
  public LocationResponse getLocation(@RequestBody LocationRequest request) {
    log.debug("Location request received : {}", request);
    LocationResponse locationResponse = locationService.getLocation(request, headers);
    log.debug("Location result ready : {}", locationResponse);
    this.auditLogger.createLocationSuccess(Collections.singletonList(locationResponse.toString()));
    return locationResponse;
  }

  // TODO: Create the permission for os-file and change pre authorize annotation
  @Hidden
  @Operation(summary = "${fileLocationApi.getFileLocation.summary}", description = "${fileLocationApi.getFileLocation.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-location-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File location", content = { @Content(schema = @Schema(implementation = FileLocationResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PostMapping("/v2/getFileLocation")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
  public FileLocationResponse getFileLocation(@RequestBody FileLocationRequest request) {
    log.debug("File location request received : {}", request);
    FileLocationResponse fileLocationResponse = locationService.getFileLocation(request, headers);
    log.debug("File location result ready : {}", fileLocationResponse);
    this.auditLogger.readFileLocationSuccess(Collections.singletonList(fileLocationResponse.toString()));
    return fileLocationResponse;
  }

  @Operation(summary = "${fileLocationApi.getLocationFile.summary}", description = "${fileLocationApi.getLocationFile.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-location-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File location", content = { @Content(schema = @Schema(implementation = LocationResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @GetMapping("/v2/files/uploadURL")
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS+ "')")
  public LocationResponse getLocationFile(@Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
      example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) throws JsonProcessingException {
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters(expiryTime);
    LocationRequest req = (new ObjectMapper()).readValue("{}", LocationRequest.class);
    LocationResponse locationResponse = locationService.getLocation(req, headers, signedUrlParameters);
    log.debug("Location result ready : {}", locationResponse);
    return locationResponse;
  }

}
