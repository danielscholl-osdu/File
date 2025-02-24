/*
 * Copyright 2020 Google LLC
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.provider.interfaces.IFileListService;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.opengroup.osdu.file.provider.interfaces.IValidationService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileListServiceImpl implements IFileListService {

  final IValidationService validationService;
  final IFileLocationRepository fileLocationRepository;

  @Override
  public FileListResponse getFileList(FileListRequest request, DpsHeaders headers) {
    log.debug("Request file list with parameters : {}", request);
    validationService.validateFileListRequest(request);

    FileListResponse response = fileLocationRepository.findAll(request);
    log.debug("File list result : {}", response);
    return response;
  }

}
