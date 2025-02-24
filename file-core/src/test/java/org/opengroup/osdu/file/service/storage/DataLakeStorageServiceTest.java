/*
 * Copyright 2021 Microsoft Corporation
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
package org.opengroup.osdu.file.service.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataLakeStorageServiceTest {

  private final String RECORD_ID = "opendes:dataset--file:data";
  private final String RECORD_ID_2 = "closedes:dataset--file:data";
  private final String KIND = "opendes:wks:dataset--file:1.0.0";
  private final String ACL_VIEWER = "data.default.viewers@opendes.contoso.com";
  private final String ACL_OWNER = "data.default.owners@opendes.contoso.com";
  private final String API_KEY = "key";
  private final String URL = "https://contoso.com";


  @Mock
  private IHttpClient httpClient;

  @Mock
  private DpsHeaders headers;

  @Mock
  private HttpResponseBodyMapper bodyMapper;

  @Mock
  private StorageAPIConfig storageAPIConfig;

  @Mock
  private HttpResponse httpResponse;

  @Mock
  private HttpResponseBodyParsingException httpResponseBodyParsingException;

  private DataLakeStorageService dataLakeStorageService;

  @BeforeEach
  public void init() {
    when(storageAPIConfig.getApiKey()).thenReturn(API_KEY);
    when(storageAPIConfig.getStorageServiceBaseUrl()).thenReturn(URL);
  }

  @Test
  public void upsertRecordReturnsSingleRecord() throws StorageException, HttpResponseBodyParsingException {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(httpResponse.isSuccessCode()).thenReturn(true);
    when(bodyMapper.parseBody(httpResponse, UpsertRecords.class)).thenReturn(getUpsertRecordObject(Collections.singletonList(RECORD_ID)));

    UpsertRecords result = dataLakeStorageService.upsertRecord(getRecord(RECORD_ID, KIND));
    assertNotNull(result);
    assertEquals(RECORD_ID, result.getRecordIds().get(0));

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(bodyMapper, times(1)).parseBody(httpResponse, UpsertRecords.class);
    verify(httpResponse, times(1)).isSuccessCode();

  }

  @Test
  public void upsertRecordThrowsStorageException() {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(httpResponse.isSuccessCode()).thenReturn(false);

    StorageException exception = assertThrows(StorageException.class, () -> {
      dataLakeStorageService.upsertRecord(getRecord(RECORD_ID, KIND));
    });
    Assertions.assertEquals("Error making request to Storage service. Check the inner HttpResponse for more info.", exception.getMessage());

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(httpResponse, times(1)).isSuccessCode();

  }

  @Test
  public void upsertRecordThrowsStorageExceptionInBodyParsing() throws HttpResponseBodyParsingException {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(bodyMapper.parseBody(httpResponse, UpsertRecords.class)).thenThrow(httpResponseBodyParsingException);
    when(httpResponse.isSuccessCode()).thenReturn(true);

    StorageException exception = assertThrows(StorageException.class, () -> {
      dataLakeStorageService.upsertRecord(getRecord(RECORD_ID, KIND));
    });
    Assertions.assertEquals("Error parsing response. Check the inner HttpResponse for more info.", exception.getMessage());

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(httpResponse, times(1)).isSuccessCode();
    verify(bodyMapper, times(1)).parseBody(httpResponse, UpsertRecords.class);

  }

  @Test
  public void upsertRecordReturnsMultipleRecords() throws StorageException, HttpResponseBodyParsingException {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(httpResponse.isSuccessCode()).thenReturn(true);
    when(bodyMapper.parseBody(httpResponse, UpsertRecords.class)).thenReturn(getUpsertRecordObject(Arrays.asList(RECORD_ID, RECORD_ID_2)));

    UpsertRecords result = dataLakeStorageService.upsertRecord(new Record[]{ getRecord(RECORD_ID, KIND), getRecord(RECORD_ID_2, KIND) });
    assertNotNull(result);
    assertEquals(RECORD_ID, result.getRecordIds().get(0));
    assertEquals(RECORD_ID_2, result.getRecordIds().get(1));

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(bodyMapper, times(1)).parseBody(httpResponse, UpsertRecords.class);
    verify(httpResponse, times(1)).isSuccessCode();

  }

  @Test
  public void getRecordSuccess() throws StorageException, HttpResponseBodyParsingException {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(httpResponse.isSuccessCode()).thenReturn(true);
    when(httpResponse.IsNotFoundCode()).thenReturn(false);
    when(bodyMapper.parseBody(httpResponse, Record.class)).thenReturn(getRecord(RECORD_ID, KIND));

    Record result = dataLakeStorageService.getRecord(RECORD_ID);
    assertNotNull(result);
    assertEquals(RECORD_ID, result.getId());

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(bodyMapper, times(1)).parseBody(httpResponse, Record.class);
    verify(httpResponse, times(1)).isSuccessCode();
    verify(httpResponse, times(1)).IsNotFoundCode();

  }

  @Test
  public void getRecordsSuccess() throws StorageException, HttpResponseBodyParsingException {

    MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);
    when(httpResponse.isSuccessCode()).thenReturn(true);
    when(httpResponse.IsNotFoundCode()).thenReturn(false);
    when(bodyMapper.parseBody(httpResponse, MultiRecordInfo.class)).thenReturn(multiRecordInfo);

    MultiRecordInfo result = dataLakeStorageService.getRecords(Arrays.asList(RECORD_ID, RECORD_ID_2));
    assertNotNull(result);

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));
    verify(bodyMapper, times(1)).parseBody(httpResponse, MultiRecordInfo.class);
    verify(httpResponse, times(1)).isSuccessCode();
    verify(httpResponse, times(1)).IsNotFoundCode();

  }

  @Test
  public void deleteRecordSuccess() {

    dataLakeStorageService = new DataLakeStorageService(storageAPIConfig, httpClient, headers, bodyMapper);
    when(httpClient.send(any(HttpRequest.class))).thenReturn(httpResponse);

    HttpResponse result = dataLakeStorageService.deleteRecord(RECORD_ID);
    assertNotNull(result);

    verify(storageAPIConfig, times(2)).getApiKey();
    verify(storageAPIConfig, times(1)).getStorageServiceBaseUrl();
    verify(headers, times(1)).getHeaders();
    verify(headers, times(1)).put("AppKey", API_KEY);
    verify(httpClient, times(1)).send(any(HttpRequest.class));

  }
  private Record getRecord(String recordId, String kind) {
    Record record = new Record();
    record.setId(recordId);
    record.setKind(kind);
    record.setAcl(new Acl(new String[]{ACL_VIEWER}, new String[]{ACL_OWNER}));

    return record;
  }

  private UpsertRecords getUpsertRecordObject(List<String> ids) {
    UpsertRecords record = new UpsertRecords();
    record.setRecordIds(ids);
    return record;
  }
}
