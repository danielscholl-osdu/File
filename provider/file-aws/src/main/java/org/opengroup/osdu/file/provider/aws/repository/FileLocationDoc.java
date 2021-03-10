/*
 * Copyright 2020 Amazon Web Services
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

package org.opengroup.osdu.file.provider.aws.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "FileLocationRepository") // DynamoDB table name (without environment prefix)
public class FileLocationDoc {

  @DynamoDBHashKey(attributeName = "fileId")
  private String fileId;

  @DynamoDBRangeKey(attributeName = "dataPartitionId")
  private String dataPartitionId;

  @DynamoDBAttribute(attributeName = "driver")
  private String driver;

  @DynamoDBAttribute(attributeName = "location")
  private String location;

  @DynamoDBAttribute(attributeName = "createdAt")
  @DynamoDBTypeConverted(converter = DateToEpochTypeConverter.class)
  private Date createdAt;

  @DynamoDBAttribute(attributeName = "createdBy")
  private String createdBy;

  public FileLocation createFileLocationFromDoc(){
    FileLocation fileLocation = FileLocation.builder()
                                .fileID(fileId)
                                .driver(DriverType.valueOf(driver))
                                .location(location)
                                .createdAt(createdAt)
                                .createdBy(createdBy)
                                .build();
    
    return fileLocation;
  }

  public static FileLocationDoc createFileLocationDoc(FileLocation fileLocation, String dataPartitionId) {
      return FileLocationDoc.builder()
          .fileId(fileLocation.getFileID())
          .dataPartitionId(dataPartitionId)
          .driver(fileLocation.getDriver().name())
          .location(fileLocation.getLocation())
          .createdAt(fileLocation.getCreatedAt())
          .createdBy(fileLocation.getCreatedBy())
          .build();
  }
}
