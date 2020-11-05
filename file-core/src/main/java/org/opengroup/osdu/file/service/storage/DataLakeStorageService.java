package org.opengroup.osdu.file.service.storage;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.model.storage.UpsertRecords;
import com.google.gson.JsonSyntaxException;

public class DataLakeStorageService {
    private final String rootUrl;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;

    DataLakeStorageService(
            StorageAPIConfig config,
            IHttpClient httpClient,
            DpsHeaders headers) {
        this.rootUrl = config.getRootUrl();
        this.httpClient = httpClient;
        this.headers = headers;
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
        HttpResponse result = this.httpClient.send(
                HttpRequest.put(records).url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, UpsertRecords.class);
    }

    public Record getRecord(String id) throws StorageException {
        String url = this.createUrl(String.format("/records/%s", id));
        HttpResponse result = this.httpClient.send(
                HttpRequest.get().url(url).headers(this.headers.getHeaders()).build());
        return result.IsNotFoundCode() ? null : this.getResult(result, Record.class);
    }


    private StorageException generateException(HttpResponse result) {
        return new StorageException(
                "Error making request to Storage service. Check the inner HttpResponse for more info.", result);
    }

    private String createUrl(String pathAndQuery) {
        return StringUtils.join(this.rootUrl, pathAndQuery);
    }

    private <T> T getResult(HttpResponse result, Class<T> type) throws StorageException {
        if (result.isSuccessCode()) {
            try {
                return result.parseBody(type);
            } catch (JsonSyntaxException e) {
                throw new StorageException("Error parsing response. Check the inner HttpResponse for more info.",
                                           result);
            }
        } else {
            throw this.generateException(result);
        }
    }

}
