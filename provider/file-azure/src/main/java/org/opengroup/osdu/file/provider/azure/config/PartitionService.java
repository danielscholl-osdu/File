package org.opengroup.osdu.file.provider.azure.config;

import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;

@Component
@RequestScope
public class PartitionService {

  private final String storageAccount;

  private final String storageAccountKey;

  @Autowired
  private final PartitionServiceClient partitionServiceClient;

  @Inject
  public PartitionService(DpsHeaders headers, PartitionServiceClient partitionServiceClient) {
    this.partitionServiceClient = partitionServiceClient;
    String dataPartitionId = headers.getPartitionId();
    Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

    PartitionInfoAzure partitionInfo = this.partitionServiceClient.getPartition(dataPartitionId);
    storageAccount = partitionInfo.getStorageAccountName();
    storageAccountKey = partitionInfo.getStorageAccountKey();
  }

  public String getStorageAccount() {
    return storageAccount;
  }

  public String getStorageAccountKey() {
    return storageAccountKey;
  }
}
