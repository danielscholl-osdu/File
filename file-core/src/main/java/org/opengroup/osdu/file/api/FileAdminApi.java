package org.opengroup.osdu.file.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.file.constant.FileServiceRole;
import org.opengroup.osdu.file.service.IFileAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Map;

@RestController
@RequestScope
@Validated
@RequiredArgsConstructor
@Hidden
public class FileAdminApi {

  final IFileAdminService fileAdminService;


  @PreAuthorize("@authorizationFilter.hasPermission('" + FileServiceRole.ADMIN + "')")
  @DeleteMapping("/v2/files/revokeURL")
  public ResponseEntity<Void> revokeURL(@RequestBody Map<String, String> revokeURLRequest) {
      fileAdminService.revokeUrl(revokeURLRequest);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
