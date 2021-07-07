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
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.validation.annotation.Validated;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
public class FileDmsApi {

  final DpsHeaders headers;

  @Autowired
  @Qualifier("FileDmsService")
  private IDmsService fileDmsService;

  @PostMapping("/v2/files/storage")
  public StorageInstructionsResponse getStorageInstructions() {
    return fileDmsService.getStorageInstructions();
  }

  @PostMapping("/v2/files/retrieval")
  public RetrievalInstructionsResponse getRetrievalInstructions(
      @RequestBody RetrievalInstructionsRequest retrievalInstructionsRequest) {
    return fileDmsService.getRetrievalInstructions(retrievalInstructionsRequest);
  }
}
