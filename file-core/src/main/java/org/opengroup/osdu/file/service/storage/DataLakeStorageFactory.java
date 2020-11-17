package org.opengroup.osdu.file.service.storage;

import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

public class DataLakeStorageFactory {

    private final StorageAPIConfig config;

    public DataLakeStorageFactory(StorageAPIConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("StorageAPIConfig cannot be empty");
        }
        this.config = config;
    }

    public DataLakeStorageService create(DpsHeaders headers) {
        if (headers == null) {
            throw new NullPointerException("headers cannot be null");
        }
        return new DataLakeStorageService(this.config, new HttpClient(), headers);
    }
}
