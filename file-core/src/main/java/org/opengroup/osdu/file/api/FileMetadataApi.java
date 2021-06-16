package org.opengroup.osdu.file.api;

import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.exception.NotFoundException;
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
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/v2/files")
@RequiredArgsConstructor
public class FileMetadataApi {

    final FileMetadataService fileMetadataService;

    @PostMapping("/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
    public ResponseEntity<FileMetadataResponse> postFilesMetadata(
            @Validated(FileMetadataValidationSequence.class) @RequestBody FileMetadata fileMetadata)
            throws OsduBadRequestException, StorageException, ApplicationException {
        FileMetadataResponse response = fileMetadataService.saveMetadata(fileMetadata);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.VIEWERS + "')")
    public ResponseEntity<RecordVersion> getFileMetadataById(@PathVariable("id") String id)
            throws OsduBadRequestException, ApplicationException, NotFoundException, StorageException {
        return new ResponseEntity<>(fileMetadataService.getMetadataById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/metadata")
    @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.EDITORS + "')")
    public ResponseEntity<Void> deleteFileMetadataById(@PathVariable("id") String id)
            throws OsduBadRequestException, ApplicationException, NotFoundException, StorageException {
        fileMetadataService.deleteMetadataRecord(id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
