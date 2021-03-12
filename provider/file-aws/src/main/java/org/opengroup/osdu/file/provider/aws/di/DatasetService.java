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

package org.opengroup.osdu.file.provider.aws.di;

import java.util.ArrayList;
import java.util.HashMap;

import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.util.UrlNormalizationUtil;
import org.opengroup.osdu.file.provider.aws.di.model.CreateDatasetRegistryRequest;
import org.opengroup.osdu.file.provider.aws.di.model.GetCreateUpdateDatasetRegistryResponse;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetRegistryRequest;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetStorageInstructionsResponse;

public class DatasetService implements IDatasetService {

    private final String rootUrl;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;
    private final HttpResponseBodyMapper bodyMapper;

    public DatasetService(DatasetAPIConfig config, HttpClient httpClient, DpsHeaders headers,
            HttpResponseBodyMapper bodyMapper) {

        this.rootUrl = config.getRootUrl();
        this.httpClient = httpClient;
        this.headers = headers;
        this.bodyMapper = bodyMapper;
        if (config.apiKey != null) {
            headers.put("AppKey", config.getApiKey());
        }

    }

    @Override
    public GetCreateUpdateDatasetRegistryResponse getDatasetRegistry(String datasetRegistryId) throws DatasetException { 

        ArrayList<String> datasetRegistryIds = new ArrayList<>();
        datasetRegistryIds.add(datasetRegistryId);

        return getDatasetRegistry(datasetRegistryIds);
    
    }

    @Override
    public GetCreateUpdateDatasetRegistryResponse getDatasetRegistry(ArrayList<String> datasetRegistryIds) throws DatasetException { 

        String url = this.createUrl("/getDatasetRegistry");

        GetDatasetRegistryRequest request = new GetDatasetRegistryRequest(datasetRegistryIds);

        HttpResponse result = this.httpClient.send(
                HttpRequest.post(request).url(url).headers(this.headers.getHeaders()).build());
        
        return this.getResult(result, GetCreateUpdateDatasetRegistryResponse.class);
    
    }

    @Override
    public GetCreateUpdateDatasetRegistryResponse registerDataset(Record datasetRecord) throws DatasetException {
        ArrayList<Record> datasetRecords = new ArrayList<>();
        datasetRecords.add(datasetRecord);

        return this.registerDataset(datasetRecords);
    }

    @Override
    public GetCreateUpdateDatasetRegistryResponse registerDataset(ArrayList<Record> datasetRecords) throws DatasetException {
        
        String url = this.createUrl("/registerDataset");
        CreateDatasetRegistryRequest request = new CreateDatasetRegistryRequest(datasetRecords);
        
        HttpResponse result = this.httpClient.send(
                HttpRequest.put(request).url(url).headers(this.headers.getHeaders()).build());
        
        return this.getResult(result, GetCreateUpdateDatasetRegistryResponse.class);
    }

    @Override
    public GetDatasetStorageInstructionsResponse getStorageInstructions(String kindSubType) throws DatasetException {
        
        String url = this.createUrl("/getStorageInstructions");
        // HashMap<String, String> queryParams = new HashMap<>();
        // queryParams.put("kindSubType", kindSubType);

        String urlWithQuery = String.format("%s?kindSubType=%s", url, kindSubType);

        HttpResponse result = this.httpClient
                .send(HttpRequest.get().url(urlWithQuery).headers(this.headers.getHeaders()).build());
        
        return this.getResult(result, GetDatasetStorageInstructionsResponse.class);
    }

    @Override
    public GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(String datasetRegistryId) throws DatasetException {
        
        ArrayList<String> datasetRegistryIds = new ArrayList<>();
        datasetRegistryIds.add(datasetRegistryId);

        return getRetrievalInstructions(datasetRegistryIds);

    }

    @Override
    public GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(ArrayList<String> datasetRegistryIds) throws DatasetException {
        
        String url = this.createUrl("/getRetrievalInstructions");

        GetDatasetRegistryRequest request = new GetDatasetRegistryRequest(datasetRegistryIds);
        
        HttpResponse result = this.httpClient.send(
                HttpRequest.post(request).url(url).headers(this.headers.getHeaders()).build());
        
        return this.getResult(result, GetDatasetRetrievalInstructionsResponse.class);
    }

    private <T> T getResult(HttpResponse result, Class<T> type) throws DatasetException {
        if (result.isSuccessCode()) {
            try {
                return bodyMapper.parseBody(result, type);
            } catch (HttpResponseBodyParsingException e) {
                throw new DatasetException("Error parsing Dataset Service response. Check the inner HttpResponse for more info.",
                        result);
            }
        } else {
            throw this.generateException(result);
        }
    }

    private DatasetException generateException(HttpResponse result) {
        return new DatasetException(
                "Error making request to Dataset service. Check the inner HttpResponse for more info.", result);
    }

    private String createUrl(String pathAndQuery) {
        return UrlNormalizationUtil.normalizeStringUrl(this.rootUrl,pathAndQuery);
    }
    
}
