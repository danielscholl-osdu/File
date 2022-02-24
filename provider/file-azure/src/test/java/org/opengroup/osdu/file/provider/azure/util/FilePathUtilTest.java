/*
 * Copyright 2020  Microsoft Corporation
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
package org.opengroup.osdu.file.provider.azure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class FilePathUtilTest {

  @InjectMocks
  FilePathUtil filePathUtil;

  @Test
  void normalizeFilePath_ShouldThrow_ForBlankFilePath() {
    // given
    String[] invalidFilePaths = {"", "    ", null};
    for(String filePath: invalidFilePaths) {
      // when
      Throwable thrown = catchThrowable(()->filePathUtil.normalizeFilePath(filePath));
      // then
      then(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(String.format("Relative file path received %s", filePath));
    }
  }

  @Test
  void normalizeFilePath_ShouldRemove_LeadingAndTrailingSlashes() {
    String filePathWithLeadingAndTrailingSlash = "/osdu/file/";
    String expectedFilePath = "osdu/file";
    String actualFilePath = filePathUtil.normalizeFilePath(filePathWithLeadingAndTrailingSlash);
    assertEquals(expectedFilePath, actualFilePath);
  }

  @Test
  void normalizeFilePath_ShouldRemove_DuplicateSlashes() {
    String filePathWithDuplicateSlash = "osdu//file";
    String expectedFilePath = "osdu/file";
    String actualFilePath = filePathUtil.normalizeFilePath(filePathWithDuplicateSlash);
    assertEquals(expectedFilePath, actualFilePath);
  }

  @Test
  void normalizeFilePath_ShouldRemove_DuplicateSlashes_And_TrailingOrLeadingSlashes() {
    String[] testFilePaths = {"//osdu//file/////", "osdu///file//", "//osdu/file"};
    for(String filePath: testFilePaths) {
      String expectedFilePath = "osdu/file";
      String actualFilePath = filePathUtil.normalizeFilePath(filePath);
      assertEquals(expectedFilePath, actualFilePath);
    }
  }
}
