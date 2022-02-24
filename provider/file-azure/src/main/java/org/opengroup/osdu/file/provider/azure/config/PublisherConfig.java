package org.opengroup.osdu.file.provider.azure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class PublisherConfig {

    @Value("${azure.publisher.batchSize}")
    private String pubSubBatchSize;
}
