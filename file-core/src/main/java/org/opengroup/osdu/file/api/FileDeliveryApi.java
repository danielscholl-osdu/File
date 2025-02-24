package org.opengroup.osdu.file.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.service.FileDeliveryService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@RestController
@RequestScope
@Validated
@RequiredArgsConstructor
@Tag(name = "file-delivery-api", description = "File Delivery API")
public class FileDeliveryApi {

  final FileDeliveryService fileDeliveryService;


  // TODO: Create the permission for os-file and change pre authorize annotation
  @Operation(summary = "${fileDeliveryApi.downloadURL.summary}", description = "${fileDeliveryApi.downloadURL.description}",
      security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-delivery-api" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "A successful response", content = { @Content(schema = @Schema(implementation = DownloadUrlResponse.class))}),
      @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
      @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
  })
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.VIEWERS + "')")
  @GetMapping("/v2/files/{id}/downloadURL")
  public ResponseEntity<DownloadUrlResponse> downloadURL(
      @Parameter(description = "File Metadata record Id.") @PathVariable("id") String id,
      @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
        example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime)
      throws StorageException {

      SignedUrlParameters params = new SignedUrlParameters(expiryTime);
      DownloadUrlResponse signedUrl = fileDeliveryService.getSignedUrlsByRecordId(id, params);
      return new ResponseEntity<>(signedUrl, HttpStatus.OK);
  }

}
