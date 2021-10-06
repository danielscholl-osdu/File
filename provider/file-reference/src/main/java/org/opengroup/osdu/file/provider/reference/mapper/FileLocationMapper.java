/*
 * Copyright 2021 Google LLC
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

package org.opengroup.osdu.file.provider.reference.mapper;

import org.mapstruct.Mapper;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.file.provider.reference.model.entity.FileLocationDocument;

@Mapper
public abstract class FileLocationMapper {

  /**
   * Map file location Mongo document to file location model.
   *
   * @param document file location document
   * @return file location
   */
  public FileLocation toFileLocation(FileLocationDocument document) {
    if (document == null) {
      return null;
    }

    return FileLocation.builder()
        .fileID(document.getFileID())
        .driver(document.getDriver() != null ? DriverType.valueOf(document.getDriver()) : null)
        .location(document.getLocation())
        .createdAt(document.getCreatedAt())
        .createdBy(document.getCreatedBy())
        .build();
  }

  /**
   * Map file location model to file location Mongo document.
   *
   * @param fileLocation file location
   * @return file location Mongo document
   */
  public abstract FileLocationDocument toDocument(FileLocation fileLocation);
}
