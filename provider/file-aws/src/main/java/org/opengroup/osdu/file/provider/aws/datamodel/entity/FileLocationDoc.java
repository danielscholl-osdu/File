/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.file.provider.aws.datamodel.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.file.provider.aws.datamodel.coverter.DateToEpochTypeConverter;

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

    public FileLocation createFileLocationFromDoc() {
        return FileLocation.builder()
                           .fileID(fileId)
                           .driver(DriverType.valueOf(driver))
                           .location(location)
                           .createdAt(createdAt)
                           .createdBy(createdBy)
                           .build();
    }
}
