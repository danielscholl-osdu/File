/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
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

package org.opengroup.osdu.file.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import org.apache.commons.lang3.RandomStringUtils;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.file.apitest.Config;

public class FileUtils {

  private static final String FILE_ID = "file-integration-test-";

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public static String generateUniqueFileID() {
    return FILE_ID + RandomStringUtils.randomAlphanumeric(10).toLowerCase();
  }

  public static String generateFileRequestBody(String fileId) throws JsonProcessingException {
    LocationRequest locationRequest = LocationRequest.builder()
        .fileID(fileId)
        .build();
    return mapper.writerFor(LocationRequest.class)
        .writeValueAsString(locationRequest);
  }

  public static String generateFileListRequestBody(LocalDateTime from, LocalDateTime to,
      int pageNum, short items) throws JsonProcessingException {

    FileListRequest fileListRequest = FileListRequest.builder()
        .timeFrom(from)
        .timeTo(to)
        .pageNum(pageNum)
        .items(items)
        .userID(Config.getUserId())
        .build();

    return mapper.writerFor(FileListRequest.class)
        .writeValueAsString(fileListRequest);
  }
}
