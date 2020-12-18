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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "FileLocation") // DynamoDB table name (without environment prefix)
public class FileLocationDoc {

  @DynamoDBHashKey(attributeName = "fileId")
  private String fileId;

  @DynamoDBAttribute(attributeName = "driver")
  private String driver;

  @DynamoDBAttribute(attributeName = "location")
  private String location;

  @DynamoDBAttribute(attributeName = "unsignedLocation")
  private String unsignedLocation;

  @DynamoDBAttribute(attributeName = "createdAt")
  private Date createdAt;

  @DynamoDBAttribute(attributeName = "createdBy")
  private String createdBy;
}
