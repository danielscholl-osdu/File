/*
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.file.service;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.util.UrlNormalizationUtil;
import org.opengroup.osdu.file.exception.OsduException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;



@Component
@RequiredArgsConstructor
public class RecordServiceImpl {

  private IHttpClient httpClient;

  @PostConstruct
  public void init(){
    this.httpClient = new HttpClient();
  }

  @Value("${RECORDS_ROOT_URL}")
  String rootUrl;

	private final HttpResponseBodyMapper bodyMapper;

  public Map<String, Object> createOrUpdateRecord(Record recordToHandle, DpsHeaders headers) {
    Record[] records = new Record[1];
    records[0] = recordToHandle;
    String url = this.createUrl("/records");
    HttpResponse result = this.httpClient.send(
        HttpRequest.put(records).url(url).headers(headers.getHeaders()).build());
    return this.getResult(result, Map.class);
  }

  private OsduException generateException(HttpResponse result)  {
    return new OsduException(String.format("Error making request to Storage service: %s", result.getBody()));
  }

  private String createUrl(String pathAndQuery) {
    return UrlNormalizationUtil.normalizeStringUrl(this.rootUrl,pathAndQuery);
  }

  private <T> T getResult(HttpResponse result, Class<T> type) throws OsduException {
    if (result.isSuccessCode()) {
        try {
            return bodyMapper.parseBody(result, type);
        } catch (HttpResponseBodyParsingException e) {
            throw new OsduException("Problem parsing response from storage service", e);
        }
    } else {
        throw this.generateException(result);
    }
  }
}
