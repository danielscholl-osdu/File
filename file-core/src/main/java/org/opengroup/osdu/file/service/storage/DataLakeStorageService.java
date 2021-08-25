package org.opengroup.osdu.file.service.storage;

import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordIds;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.util.UrlNormalizationUtil;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;

import java.util.ArrayList;
import java.util.Collection;

public class DataLakeStorageService {
    private final String storageServiceBaseUrl;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;
    private final HttpResponseBodyMapper bodyMapper;

    DataLakeStorageService(StorageAPIConfig config, IHttpClient httpClient, DpsHeaders headers,
            HttpResponseBodyMapper bodyMapper) {
        this.storageServiceBaseUrl = config.getStorageServiceBaseUrl();
        this.httpClient = httpClient;
        this.headers = headers;
        this.bodyMapper = bodyMapper;
        if (config.getApiKey() != null) {
            headers.put("AppKey", config.getApiKey());
        }
    }

    public UpsertRecords upsertRecord(Record record) throws StorageException {
        Record[] records = new Record[1];
        records[0] = record;
        return this.upsertRecord(records);
    }

    public UpsertRecords upsertRecord(Record[] records) throws StorageException {
        String url = this.createUrl("/records");
        HttpResponse result = this.httpClient
                .send(HttpRequest.put(records).url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, UpsertRecords.class);
    }

    public Record getRecord(String id) throws StorageException {
        String url = this.createUrl(String.format("/records/%s", id));
        HttpResponse result = this.httpClient
                .send(HttpRequest.get().url(url).headers(this.headers.getHeaders()).build());
        return result.IsNotFoundCode() ? null : this.getResult(result, Record.class);
    }

    public HttpResponse deleteRecord(String id) {
        String url = this.createUrl(String.format("/records/%s:delete", id));
        HttpResponse result = this.httpClient
                .send(HttpRequest.post("{'anything':'anything'}").url(url).headers(this.headers.getHeaders()).build());
        return result;
    }

    public MultiRecordInfo getRecords(Collection<String> ids) throws StorageException {
        MultiRecordIds input = new MultiRecordIds();
        input.setRecords(new ArrayList());
        input.getRecords().addAll(ids);
        String url = this.createUrl("/query/records");
        HttpResponse result = this.httpClient.send(HttpRequest.post(input).url(url).headers(this.headers.getHeaders()).build());
        return result.IsNotFoundCode() ? null : this.getResult(result, MultiRecordInfo.class);
    }

    private StorageException generateException(HttpResponse result) {
        return new StorageException(
                "Error making request to Storage service. Check the inner HttpResponse for more info.", result);
    }

    private String createUrl(String pathAndQuery) {
        return UrlNormalizationUtil.normalizeStringUrl(this.storageServiceBaseUrl, pathAndQuery);
    }


    private <T> T getResult(HttpResponse result, Class<T> type) throws StorageException {
        if (result.isSuccessCode()) {
            try {
                return bodyMapper.parseBody(result, type);
            } catch (HttpResponseBodyParsingException e) {
                throw new StorageException("Error parsing response. Check the inner HttpResponse for more info.",
                        result);
            }
        } else {
            throw this.generateException(result);
        }
    }

}
