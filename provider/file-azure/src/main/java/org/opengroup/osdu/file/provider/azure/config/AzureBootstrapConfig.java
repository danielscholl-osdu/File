//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.file.provider.azure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;

@Configuration
public class AzureBootstrapConfig {
  @Value("${azure.keyvault.url}")
  private String keyVaultURL;

  @Value("${azure.cosmosdb.database}")
  private String cosmosDBName;

  @Value("${azure.application-insights.instrumentation-key}")
  private String appInsightsKey;

  @Value("${spring.application.name}")
  private String springAppName;

  @Bean
  @Named("APPINSIGHTS_KEY")
  public String appInsightsKey() {
    return appInsightsKey;
  }

  @Bean
  @Named("spring.application.name")
  public String springAppName() {
    return springAppName;
  }

  @Bean
  @Named("COSMOS_DB_NAME")
  public String cosmosDBName() {
    return cosmosDBName;
  }

  @Bean
  @Named("KEY_VAULT_URL")
  public String keyVaultURL() {
    return keyVaultURL;
  }

  @Bean
  @Named("COSMOS_ENDPOINT")
  public String cosmosEndpoint(SecretClient kv) {
    return getKeyVaultSecret(kv, "opendes-cosmos-endpoint");
  }

  @Bean
  @Named("COSMOS_KEY")
  public String cosmosKey(SecretClient kv) {
    return getKeyVaultSecret(kv, "opendes-cosmos-primary-key");
  }

  @Bean
  @Named("AZURE_STORAGE_ACCOUNT")
  public String storageAccount(SecretClient kv) {
    String accountName = getKeyVaultSecret(kv, "opendes-storage");
    // this needs to be set because there is  static method that needs this value
    System.setProperty("azure_storage.account", accountName);
    return getKeyVaultSecret(kv, "opendes-storage");
  }


  String getKeyVaultSecret(SecretClient kv, String secretName) {
    KeyVaultSecret secret = kv.getSecret(secretName);
    if (secret == null) {
      throw new IllegalStateException(String.format("No secret found with name %s", secretName));
    }

    String secretValue = secret.getValue();
    if (secretValue == null) {
      throw new IllegalStateException(String.format(
          "Secret unexpectedly missing from KeyVault response for secret with name %s", secretName));
    }

    return secretValue;
  }

  @Bean
  public CosmosClient buildCosmosClient(SecretClient kv) {
    String cosmosEndpoint = getKeyVaultSecret(kv, "opendes-cosmos-endpoint");
    String cosmosPrimaryKey = getKeyVaultSecret(kv, "opendes-cosmos-primary-key");
    return new CosmosClientBuilder().endpoint(cosmosEndpoint).key(cosmosPrimaryKey).buildClient();
  }
}
