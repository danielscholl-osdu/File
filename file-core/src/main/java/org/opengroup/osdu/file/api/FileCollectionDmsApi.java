package org.opengroup.osdu.file.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.CopyDmsRequest;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(value = "/v2/file-collections")
public class FileCollectionDmsApi {

  @Autowired
  @Qualifier("FileCollectionDmsService")
  IDmsService fileCollectionDmsService;

  @PostMapping("/storageInstructions")
  public ResponseEntity<StorageInstructionsResponse> getStorageInstructions() {
    StorageInstructionsResponse storageInstructionsResp = fileCollectionDmsService.getStorageInstructions();
    return new ResponseEntity<>(storageInstructionsResp, HttpStatus.OK);
  }

  @PostMapping("/retrievalInstructions")
  public ResponseEntity<RetrievalInstructionsResponse> getRetrievalInstructions(
      @RequestBody RetrievalInstructionsRequest retrievalInstructionsRequest) {
    RetrievalInstructionsResponse retrievalInstructionsResp = fileCollectionDmsService.getRetrievalInstructions(retrievalInstructionsRequest);
    return new ResponseEntity<>(retrievalInstructionsResp, HttpStatus.OK);
  }

  @PostMapping("/copy")
  public ResponseEntity<List<CopyDmsResponse>> copyDms(
      @RequestBody CopyDmsRequest copyDmsRequest) {
    List<CopyDmsResponse> copyOpResponse = fileCollectionDmsService.copyDatasetsToPersistentLocation(copyDmsRequest.getDatasetSources());
    return new ResponseEntity<>(copyOpResponse, HttpStatus.OK);
  }
}
