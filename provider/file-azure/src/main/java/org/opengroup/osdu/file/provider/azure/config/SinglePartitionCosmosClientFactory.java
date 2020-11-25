package org.opengroup.osdu.file.provider.azure.config;

import com.azure.cosmos.CosmosClient;
import org.opengroup.osdu.azure.cosmosdb.ICosmosClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SinglePartitionCosmosClientFactory implements ICosmosClientFactory {

  @Autowired
  CosmosClient cosmosClient;

  @Override
  public CosmosClient getClient(final String s) {
    return cosmosClient;
  }
}
