/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.gcp.model.entity;

import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class FileLocationOsm {

  private Long id;
  private String fileID;
  private DriverType driver;
  private String location;
  private Long createdAt;
  private String createdBy;

  public FileLocationOsm(FileLocation fileLocation, Long id) {
    this.setLocation(fileLocation.getLocation());
    this.setFileID(fileLocation.getFileID());
    this.setCreatedAt(fileLocation.getCreatedAt().getTime());
    this.setCreatedBy(fileLocation.getCreatedBy());
    this.setDriver(fileLocation.getDriver());
    this.id = id;
  }

  public FileLocation toFileLocation() {
    return FileLocation.builder()
        .fileID(fileID)
        .driver(driver)
        .location(location)
        .createdAt(new Date(createdAt))
        .createdBy(createdBy)
        .build();
  }

}
