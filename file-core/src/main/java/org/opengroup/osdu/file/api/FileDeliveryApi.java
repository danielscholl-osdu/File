package org.opengroup.osdu.file.api;

import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.service.FileDeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import lombok.RequiredArgsConstructor;

@RestController
@RequestScope
@Validated
@RequiredArgsConstructor
public class FileDeliveryApi {

  final FileDeliveryService fileDeliveryService;

  // TODO: Create the permission for os-file and change pre authorize annotation
  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.VIEWERS + "')")
  @GetMapping("/v2/files/{id}/downloadURL")
  public ResponseEntity<DownloadUrlResponse> downloadURL(@PathVariable("id") String id) {
    DownloadUrlResponse signedUrl = fileDeliveryService.getSignedUrlsByRecordId(id);
    return new ResponseEntity<>(signedUrl, HttpStatus.OK);
  }

}
