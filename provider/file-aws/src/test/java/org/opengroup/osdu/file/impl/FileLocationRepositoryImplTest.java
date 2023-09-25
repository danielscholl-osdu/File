// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.aws.exceptions.InvalidCursorException;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduException;
import org.opengroup.osdu.file.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.file.provider.aws.datamodel.entity.FileLocationDoc;
import org.opengroup.osdu.file.provider.aws.impl.FileLocationRepositoryImpl;

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;


@RunWith(MockitoJUnitRunner.class)
public class FileLocationRepositoryImplTest {

    private final String fileID = "fileID";
    private final String partitionID = "partitionID";
    private final DriverType driver = DriverType.GCS;
    private final String location = "location"; 
    private final Date createdAt = Date.from(Instant.now());
    private final String createdBy = "createdBy";
    private final String cursor = "cursor";

    @Mock
    DpsHeaders headers;

    @Mock
    DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;

    @Mock
    ProviderConfigurationBag providerConfigurationBag;

    @InjectMocks
    FileLocationRepositoryImpl repository;

    @Test
    public void testFindByFileID_null() {
        assertNull(repository.findByFileID(null));
    }

    @Test(expected = AppException.class)
    public void testFindByFileID_resourceNotFound() {

        DynamoDBQueryHelperV2 queryHelper = mock(DynamoDBQueryHelperV2.class);

        when(queryHelper.loadByPrimaryKey(any(), anyString(), any())).thenThrow(new ResourceNotFoundException(fileID));
        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(any(DpsHeaders.class), any())).thenReturn(queryHelper);

        repository.findByFileID(fileID);
    }

    @Test
    public void testFindByFileID() {

        DynamoDBQueryHelperV2 queryHelper = mock(DynamoDBQueryHelperV2.class);
        FileLocationDoc doc = mock(FileLocationDoc.class);

        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(any(DpsHeaders.class), any())).thenReturn(queryHelper);
        when(queryHelper.loadByPrimaryKey(any(), anyString(), any())).thenReturn(doc);

        assertNull(repository.findByFileID(fileID));
    }

    @Test
    public void testSave() {
        DynamoDBQueryHelperV2 queryHelper = mock(DynamoDBQueryHelperV2.class);

        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(any(DpsHeaders.class), any())).thenReturn(queryHelper);
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);

        FileLocation fileLocation = new FileLocation(fileID, driver, location, createdAt, createdBy);
        assertEquals(fileLocation, repository.save(fileLocation));
    }

    @Test(expected = OsduException.class)
    public void testFindAll_UnsupportedEncoding() throws InvalidCursorException, UnsupportedEncodingException {

        short num = 1000;

        FileListRequest request = new FileListRequest(LocalDateTime.now(), LocalDateTime.of(2040, 1, 1, 1, 0, 0), 0, num, "userID");
        DynamoDBQueryHelperV2 queryHelper = mock(DynamoDBQueryHelperV2.class);

        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(any(DpsHeaders.class), any())).thenReturn(queryHelper);
        when(queryHelper.scanPage(any(), anyInt(), any(), anyString(), anyMap())).thenThrow(new UnsupportedEncodingException());
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);

        repository.findAll(request);
    }

    @Test
    public void testFindAll() throws InvalidCursorException, UnsupportedEncodingException {

        short num = 1000;

        FileListRequest request = new FileListRequest(LocalDateTime.now(), LocalDateTime.of(2040, 1, 1, 1, 0, 0), 0, num, "userID");
        List<FileLocationDoc> results = new ArrayList<FileLocationDoc>();
        FileLocationDoc doc = mock(FileLocationDoc.class);
        DynamoDBQueryHelperV2 queryHelper = mock(DynamoDBQueryHelperV2.class);
        QueryPageResult<FileLocationDoc> docs = new QueryPageResult<FileLocationDoc>(cursor, results);

        results.add(doc);
        when(doc.createFileLocationFromDoc()).thenReturn(new FileLocation());
        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(any(DpsHeaders.class), any())).thenReturn(queryHelper);
        doReturn(docs).when(queryHelper).scanPage(any(), anyInt(), any(), anyString(), anyMap());
        //when(queryHelper.scanPage(any(), anyInt(), any(), anyString(), anyMap())).thenReturn(docs);
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionID);

        assertNotNull(repository.findAll(request));
    }
}