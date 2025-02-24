package org.opengroup.osdu.file.provider.azure.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
public class AzureConfig {

  @Primary
  @Bean
  @Lazy
  public DefaultAzureCredential defaultAzureCredential() {
    return (new DefaultAzureCredentialBuilder()).build();
  }

}
