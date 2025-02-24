package org.opengroup.osdu.file.service.storage;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StorageAPIConfig {

    String storageServiceBaseUrl;
    String apiKey;

    public static StorageAPIConfig Default() {
        return StorageAPIConfig.builder().build();
    }
}
