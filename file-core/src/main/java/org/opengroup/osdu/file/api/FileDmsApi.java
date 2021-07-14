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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.*;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
@RequestMapping(value = "/v2/files")
public class FileDmsApi {

  final DpsHeaders headers;

  @Autowired
  @Qualifier("FileDmsService")
  private IDmsService fileDmsService;

  @PostMapping("/storage")
  public ResponseEntity<StorageInstructionsResponse> getStorageInstructions() {
    StorageInstructionsResponse storageInstructionsResp = fileDmsService.getStorageInstructions();
    return new ResponseEntity<>(storageInstructionsResp, HttpStatus.OK);
  }

  @PostMapping("/retrieval")
  public ResponseEntity<RetrievalInstructionsResponse> getRetrievalInstructions(
      @RequestBody RetrievalInstructionsRequest retrievalInstructionsRequest) {
    RetrievalInstructionsResponse retrievalInstructionsResp = fileDmsService.getRetrievalInstructions(retrievalInstructionsRequest);
    return new ResponseEntity<>(retrievalInstructionsResp, HttpStatus.OK);
  }

  @PostMapping("/copy")
  public ResponseEntity<List<CopyDmsResponse>> copyDms(
      @RequestBody CopyDmsRequest copyDmsRequest) {
    List<CopyDmsResponse> copyOpResponse = fileDmsService.copyDatasetsToPersistentLocation(copyDmsRequest.getDatasetSources());
    return new ResponseEntity<>(copyOpResponse, HttpStatus.OK);
  }
}
