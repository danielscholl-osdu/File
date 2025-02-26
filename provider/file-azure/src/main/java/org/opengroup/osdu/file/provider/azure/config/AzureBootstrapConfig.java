/*
 *  Copyright © Microsoft Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.provider.azure.config;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Configuration
@Getter
public class AzureBootstrapConfig {
  @Value("${azure.keyvault.url}")
  private String keyVaultURL;

  @Value("${azure.cosmosdb.database}")
  private String cosmosDBName;

  @Value("${spring.application.name}")
  private String springAppName;

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

  private static final DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

  @Bean
  @Inject
  public StorageAccountsClient storageAccountsClient() {
    AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
    AzureResourceManager azureResourceManager = AzureResourceManager
        .authenticate(defaultCredential, azureProfile)
        .withSubscription(azureProfile.getSubscriptionId());

    return azureResourceManager
        .storageAccounts()
        .manager()
        .serviceClient()
        .getStorageAccounts();
  }

}
