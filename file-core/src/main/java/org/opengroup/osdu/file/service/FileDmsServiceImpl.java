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


package org.opengroup.osdu.file.service;

import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

@Service("FileDmsService")
@Slf4j
public class FileDmsServiceImpl implements IDmsService {
  @Inject
  private IStorageService storageService;

  @Inject
  private DpsHeaders headers;

  @Override
  public StorageInstructionsResponse getStorageInstructions() {
    String fileID = generateFileId();

    log.debug("Create the empty blob in bucket. FileID : {}", fileID);
    return storageService.createStorageInstructions(fileID, headers.getAuthorization(),
        headers.getPartitionIdWithFallbackToAccountId());
  }

  private String generateFileId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Override
  public RetrievalInstructionsResponse getRetrievalInstructions(RetrievalInstructionsRequest retrievalInstructionsRequest) {
    return null;
  }
}
