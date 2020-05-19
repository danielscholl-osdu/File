package org.opengroup.osdu.file.aws.repository;

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
