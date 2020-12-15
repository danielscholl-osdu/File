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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FileLocationRepositoryImpl implements IFileLocationRepository {

  @Value("${aws.dynamodb.table.prefix}")
  String tablePrefix;

  @Value("${aws.dynamodb.region}")
  String dynamoDbRegion;

  @Value("${aws.dynamodb.endpoint}")
  String dynamoDbEndpoint;

  @Value("${aws.dynamodb.filelocationtable")
  String filelocationTableName;

  private static final String FIND_ALL_FILTER_EXPRESSION = "createdAt BETWEEN :startDate and :endDate AND createdBy = :user";

  private DynamoDBQueryHelper queryHelper;

  @PostConstruct
  public void init() {
    this.queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
  }

  @Override
  public FileLocation findByFileID(String fileID) {

    if (true)
      throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");
    
      try {
          FileLocationDoc doc = queryHelper.loadByPrimaryKey(FileLocationDoc.class, fileID);

          FileLocation fileLocation = null;
          if (doc != null){
            fileLocation = createFileLocationFromDoc(doc);
          }
      
          return fileLocation;
      }
      catch (ResourceNotFoundException e) {
        throw new AppException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getErrorMessage());
      }
  }

  @Override
  public FileLocation save(FileLocation fileLocation) {
    if (true)
      throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");

    FileLocationDoc doc = new FileLocationDoc();
    doc.setFileId(fileLocation.getFileID());
    doc.setDriver(fileLocation.getDriver().name());
    doc.setCreatedAt(fileLocation.getCreatedAt());
    doc.setCreatedBy(fileLocation.getCreatedBy());
    doc.setLocation(fileLocation.getLocation());

    queryHelper.save(doc);

    return fileLocation;
  }


  @Override
  public FileListResponse findAll(FileListRequest request) {

      if (true)
        throw new AppException(HttpStatus.NOT_IMPLEMENTED.value(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), "NOT IMPLEMENTED");

      FileListResponse response = new FileListResponse();

      AttributeValue timeFromAV = new AttributeValue(request.getTimeFrom().toString());
      AttributeValue timeToAV = new AttributeValue(request.getTimeTo().toString());
      AttributeValue userAV = new AttributeValue(request.getUserID());

      Map<String, AttributeValue> eav = new HashMap<>();
      eav.put(":startDate", timeFromAV);
      eav.put(":endDate", timeToAV);
      eav.put(":user", userAV);

      int pageSize = request.getItems();
      int pageNum = request.getPageNum();
      String pageNumStr = String.valueOf(pageNum);
      if (pageNum <= 0)
        pageNumStr = null;
      QueryPageResult<FileLocationDoc> docs = null;
      // String cursor = setCursorToNullIfEmpty(request.getCursor());
      try {
        docs = queryHelper.scanPage(FileLocationDoc.class, pageSize, pageNumStr, FIND_ALL_FILTER_EXPRESSION, eav);
      } catch (UnsupportedEncodingException e){
        throw new OsduException(e.getMessage(), e);
      }

      if (docs != null){
        List<FileLocation> locations = new ArrayList<>();
        for(FileLocationDoc doc : docs.results){
          locations.add(createFileLocationFromDoc(doc));
      }

      response =  FileListResponse.builder()
        .content(locations)
        .size(pageSize)
        .number(pageNum)
        .numberOfElements(locations.size()).build();
    }

    return response;
  }

  private String setCursorToNullIfEmpty(String cursor){
    if (cursor == ""){
      cursor = null;
    }
    return cursor;
  }

  private FileLocation createFileLocationFromDoc(FileLocationDoc doc){
    FileLocation fileLocation = new FileLocation();
    fileLocation.setFileID(doc.getFileId());
    fileLocation.setCreatedAt(doc.getCreatedAt());
    fileLocation.setCreatedBy(doc.getCreatedBy());
    fileLocation.setDriver(DriverType.valueOf(doc.getDriver()));
    fileLocation.setLocation(doc.getLocation());
    return fileLocation;
  }

}
