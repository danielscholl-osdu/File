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
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.NotFoundException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.FileMetadataResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.service.FileMetadataService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.validation.filemetadata.FileMetadataValidationSequence;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v2/files")
@RequiredArgsConstructor
@Tag(name = "file-metadata-api", description = "File Metadata API")
public class FileMetadataApi {

    final FileMetadataService fileMetadataService;

    @Operation(summary = "${fileMetadataApi.postFilesMetadata.summary}", description = "${fileMetadataApi.postFilesMetadata.description}",
        security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-metadata-api" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Metadata created", content = { @Content(schema = @Schema(implementation = FileMetadataResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PostMapping("/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
    public ResponseEntity<FileMetadataResponse> postFilesMetadata(
            @Validated(FileMetadataValidationSequence.class) @RequestBody FileMetadata fileMetadata)
            throws OsduBadRequestException, StorageException, ApplicationException {
        FileMetadataResponse response = fileMetadataService.saveMetadata(fileMetadata);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "${fileMetadataApi.getFileMetadataById.summary}", description = "${fileMetadataApi.getFileMetadataById.description}",
        security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-metadata-api" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A successful response", content = { @Content(schema = @Schema(implementation = RecordVersion.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @GetMapping("/{id}/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.VIEWERS + "')")
    public ResponseEntity<RecordVersion> getFileMetadataById(@Parameter(description = "File metadata record Id.") @PathVariable("id") String id)
            throws OsduBadRequestException, ApplicationException, NotFoundException, StorageException {
        return new ResponseEntity<>(fileMetadataService.getMetadataById(id), HttpStatus.OK);
    }

    @Operation(summary = "${fileMetadataApi.deleteFileMetadataById.summary}", description = "${fileMetadataApi.deleteFileMetadataById.description}",
        security = {@SecurityRequirement(name = "Authorization")}, tags = { "file-metadata-api" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record deleted successfully."),
        @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "404", description = "Record Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @DeleteMapping("/{id}/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "', '" + FileServiceRole.ADMIN + "')")
    public ResponseEntity<Void> deleteFileMetadataById(@Parameter(description = "File metadata record Id.")  @PathVariable("id") String id)
            throws OsduBadRequestException, ApplicationException, NotFoundException, StorageException {
        fileMetadataService.deleteMetadataRecord(id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
