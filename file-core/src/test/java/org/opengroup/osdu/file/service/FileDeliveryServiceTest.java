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

package org.opengroup.osdu.file.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class FileDeliveryServiceTest {

  public static final String RECORD_ID = "tenant1:dataset--File.Generic:1b9dd1a8-d317-11ea-87d0-0242ac130003";

  @InjectMocks
  FileDeliveryService fileDeliveryService;
  @Mock
  DpsHeaders headers;

  @Mock
  JaxRsDpsLog log;

  @Mock
  IStorageService storageService;

  @Mock
  DataLakeStorageFactory dataLakeStorageFactory;

  @Mock
  IStorageUtilService storageUtilService;

  @Mock
  ExpiryTimeUtil expiryTimeUtil;

  @Mock
  DataLakeStorageService dataLakeStorageService;

  @Test
  public void saveMetadata_Success() throws OsduBadRequestException, StorageException, MalformedURLException {
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
    String dataPartitionId = "tenant";

    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(expiryTimeUtil.isInputPatternSupported(any())).thenReturn(true);
    when(dataLakeStorageFactory.create(headers)).thenReturn(dataLakeStorageService);
    when(dataLakeStorageService.getRecord(RECORD_ID)).thenReturn(getRecordObj());
    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(headers.getAuthorization()).thenReturn("bearer");
    when(storageUtilService.getPersistentLocation("/xyz",dataPartitionId)).thenReturn("absolutePath");
    when(storageService.createSignedUrlFileLocation("absolutePath","bearer", signedUrlParameters)).thenReturn(getSignedUrl());
    DownloadUrlResponse downloadUrlResponse = fileDeliveryService.getSignedUrlsByRecordId(RECORD_ID,signedUrlParameters );
    assertEquals("http://testURL.com", downloadUrlResponse.getSignedUrl());
  }
  private SignedUrl getSignedUrl() throws MalformedURLException {
    SignedUrl signedUrl = new SignedUrl();
    signedUrl.setUrl(new URL("http://testURL.com"));
    return signedUrl;
  }
  private Record getRecordObj() {
    Record mockRecordVersion = new Record();
    FileData fileData = new FileData();
    DatasetProperties datasetProperties = new DatasetProperties();
    FileSourceInfo fileSourceInfo = new FileSourceInfo();
    fileSourceInfo.setFileSource("/xyz");
    fileSourceInfo.setName("testFileName.csv");
    datasetProperties.setFileSourceInfo(fileSourceInfo);
    fileData.setDatasetProperties(datasetProperties);
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> dataMap = objectMapper
        .convertValue(fileData, new TypeReference<Map<String, Object>>() {});
    mockRecordVersion.setData(dataMap);
    mockRecordVersion.setId(RECORD_ID);
    return mockRecordVersion;
  }

}
