package org.opengroup.osdu.file.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.service.IFileAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Map;

@RestController
@RequestScope
@Validated
@RequiredArgsConstructor
@Tag(name = "file-admin-api", description = "File Admin API")
public class FileAdminApi {

  final IFileAdminService fileAdminService;


  @Operation(summary = "${fileAdminApi.revokeURL.summary}", description = "${fileAdminApi.revokeURL.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-admin-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Revoked URLs successfully."),
      @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.ADMIN + "')")
  @PostMapping("/v2/files/revokeURL")
  public ResponseEntity<Void> revokeURL(@RequestBody Map<String, String> revokeURLRequest) {
      fileAdminService.revokeUrl(revokeURLRequest);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
