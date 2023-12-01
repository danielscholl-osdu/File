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

package org.opengroup.osdu.file.provider.aws.impl;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.datamodel.entity.FileLocationDoc;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class FileLocationRepositoryImpl implements IFileLocationRepository {

    private static final String FIND_ALL_FILTER_EXPRESSION = "dataPartitionId = :partitionId AND createdAt BETWEEN :startDate and :endDate AND createdBy = :user";
    private final DpsHeaders headers;
    private final DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;
    private final ProviderConfigurationBag providerConfigurationBag;

    @Autowired
    public FileLocationRepositoryImpl(DpsHeaders headers,
                                      DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory,
                                      ProviderConfigurationBag providerConfigurationBag) {
        this.headers = headers;
        this.dynamoDBQueryHelperFactory = dynamoDBQueryHelperFactory;
        this.providerConfigurationBag = providerConfigurationBag;
    }

    private DynamoDBQueryHelperV2 getFileLocationQueryHelper() {
        return dynamoDBQueryHelperFactory.getQueryHelperForPartition(headers,
                                                                     providerConfigurationBag.fileLocationTableParameterRelativePath);
    }

    @Override
    public FileLocation findByFileID(String fileID) {
        if (fileID == null) { //new file being generated
            return null;
        }

        try {
            DynamoDBQueryHelperV2 queryHelper = getFileLocationQueryHelper();
            String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();

            FileLocationDoc doc = queryHelper.loadByPrimaryKey(FileLocationDoc.class, fileID, dataPartitionId);

            FileLocation fileLocation = null;
            if (doc != null) {
                fileLocation = doc.createFileLocationFromDoc();
            }

            return fileLocation;
        } catch (ResourceNotFoundException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                   HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getErrorMessage());
        }
    }

    @Override
    public FileLocation save(FileLocation fileLocation) {
        DynamoDBQueryHelperV2 queryHelper = getFileLocationQueryHelper();
        String dataPartitionId = headers.getPartitionIdWithFallbackToAccountId();

        FileLocationDoc doc = FileLocationDoc.createFileLocationDoc(fileLocation, dataPartitionId);

        queryHelper.save(doc);

        return fileLocation;
    }

    @Override
    public FileListResponse findAll(FileListRequest request) {
        FileListResponse response = new FileListResponse();

        AttributeValue dataPartitionIdAV = new AttributeValue(headers.getPartitionIdWithFallbackToAccountId());
        AttributeValue timeFromAV = new AttributeValue().withN(dateToEpoch(request.getTimeFrom()).toString());
        AttributeValue timeToAV = new AttributeValue().withN(dateToEpoch(request.getTimeTo()).toString());
        AttributeValue userAV = new AttributeValue(request.getUserID());

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":partitionId", dataPartitionIdAV);
        eav.put(":startDate", timeFromAV);
        eav.put(":endDate", timeToAV);
        eav.put(":user", userAV);

        int pageSize = request.getItems();
        int pageNum = request.getPageNum();
        String pageNumStr = String.valueOf(pageNum);
        if (pageNum <= 0) {
            pageNumStr = null;
        }
        QueryPageResult<FileLocationDoc> docs;
        try {
            DynamoDBQueryHelperV2 queryHelper = getFileLocationQueryHelper();
            docs = queryHelper.scanPage(FileLocationDoc.class, pageSize, pageNumStr, FIND_ALL_FILTER_EXPRESSION, eav);
        } catch (UnsupportedEncodingException e) {
            throw new OsduException(e.getMessage(), e);
        }

        if (docs != null) {
            List<FileLocation> locations = new ArrayList<>();
            for (FileLocationDoc doc : docs.results) {
                locations.add(doc.createFileLocationFromDoc());
            }

            response = FileListResponse.builder()
                                       .content(locations)
                                       .size(pageSize)
                                       .number(pageNum)
                                       .numberOfElements(locations.size()).build();
        }

        return response;
    }

    private Long dateToEpoch(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
