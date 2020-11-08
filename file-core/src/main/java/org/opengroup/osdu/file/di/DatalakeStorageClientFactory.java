package org.opengroup.osdu.file.di;


import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.StorageAPIConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class DatalakeStorageClientFactory extends AbstractFactoryBean<DataLakeStorageFactory> {
    @Value("${storage.api}")
    private String api;

    @Value("${authorize.api.key}")
    private String apiKey;

    @Override
    protected DataLakeStorageFactory createInstance() throws Exception {

        return new DataLakeStorageFactory(StorageAPIConfig.builder().storageServiceBaseUrl(this.api).apiKey(this.apiKey).build());
    }

    @Override
    public Class<?> getObjectType() {
        return DataLakeStorageFactory.class;
    }
}
