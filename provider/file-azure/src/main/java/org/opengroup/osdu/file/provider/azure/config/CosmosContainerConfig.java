package org.opengroup.osdu.file.provider.azure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosContainerConfig {

    @Value("${filelocation.container.name}")
    private String fileLocationContainerName;

    @Bean
    public String fileLocationContainer(){
        return fileLocationContainerName;
    }

}
