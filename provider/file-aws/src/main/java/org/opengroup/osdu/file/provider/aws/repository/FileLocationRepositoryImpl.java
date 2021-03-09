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
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

  @Inject
  DpsHeaders headers;

  private static final String FIND_ALL_FILTER_EXPRESSION = "dataPartitionId = :partitionId AND createdAt BETWEEN :startDate and :endDate AND createdBy = :user";

  private DynamoDBQueryHelper queryHelper;

  @PostConstruct
  public void init() {
    this.queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
  }

  @Override
  public FileLocation findByFileID(String fileID) {

      if (fileID == null) { //new file being generated
        return null;
      }

      try {
          String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();

          FileLocationDoc doc = queryHelper.loadByPrimaryKey(FileLocationDoc.class, fileID, dataPartitionId);

          FileLocation fileLocation = null;
          if (doc != null){
            fileLocation = doc.createFileLocationFromDoc();
          }
      
          return fileLocation;
      }
      catch (ResourceNotFoundException e) {
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getErrorMessage());
      }
  }

  @Override
  public FileLocation save(FileLocation fileLocation) {

    String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();

    FileLocationDoc doc = FileLocationDoc.createFileLocationDoc(fileLocation, dataPartitionId);

    queryHelper.save(doc);

    return fileLocation;
  }


  @Override
  public FileListResponse findAll(FileListRequest request) {

      FileListResponse response = new FileListResponse();

      AttributeValue dataPartitionIdAV = new AttributeValue(headers.getPartitionIdWithFallbackToAccountId());
      AttributeValue timeFromAV = new AttributeValue(request.getTimeFrom().toString());
      AttributeValue timeToAV = new AttributeValue(request.getTimeTo().toString());
      AttributeValue userAV = new AttributeValue(request.getUserID());

      Map<String, AttributeValue> eav = new HashMap<>();
      eav.put(":partitionId", dataPartitionIdAV);
      eav.put(":startDate", timeFromAV);
      eav.put(":endDate", timeToAV);
      eav.put(":user", userAV);
      

      int pageSize = request.getItems();
      int pageNum = request.getPageNum();
      String pageNumStr = String.valueOf(pageNum);
      if (pageNum <= 0)
        pageNumStr = null;
      QueryPageResult<FileLocationDoc> docs = null;      
      try {
        docs = queryHelper.scanPage(FileLocationDoc.class, pageSize, pageNumStr, FIND_ALL_FILTER_EXPRESSION, eav);
      } catch (UnsupportedEncodingException e){
        throw new OsduException(e.getMessage(), e);
      }

      if (docs != null){
        List<FileLocation> locations = new ArrayList<>();
        for(FileLocationDoc doc : docs.results){
          locations.add(doc.createFileLocationFromDoc());
      }

      response =  FileListResponse.builder()
        .content(locations)
        .size(pageSize)
        .number(pageNum)
        .numberOfElements(locations.size()).build();
    }

    return response;
  }

}
