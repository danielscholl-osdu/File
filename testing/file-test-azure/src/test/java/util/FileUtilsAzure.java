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

package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.file.apitest.Config;

import java.time.LocalDateTime;

public class FileUtilsAzure {

  private static final String FILE_ID = "file-integration-test-";

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public static String generateUniqueFileID() {
    return  RandomStringUtils.randomAlphanumeric(1025).toLowerCase();
  }



  public static String generateFileListRequestBody(int pageNum,LocalDateTime from, LocalDateTime to,
                                                   short items) throws JsonProcessingException {

    FileListRequest fileListRequest = FileListRequest.builder()
        .pageNum(pageNum)
        .timeFrom(from)
        .userID("test-user")
        .timeTo(to)
        .items(items)
        .build();

    return mapper.writerFor(FileListRequest.class)
        .writeValueAsString(fileListRequest);
  }

}
