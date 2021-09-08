package org.opengroup.osdu.file.provider.azure.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.opengroup.osdu.azure.blobstorage.IBlobServiceClientFactory;
import org.opengroup.osdu.azure.partition.PartitionInfoAzure;
import org.opengroup.osdu.azure.partition.PartitionServiceClient;
import org.opengroup.osdu.common.Validators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.AZURE_PROTOCOL;
import static org.opengroup.osdu.file.provider.azure.model.constant.StorageConstant.BLOB_STORAGE_ACCOUNT_BASE_URI_REGEX;

@Component
@Primary
class MultiPartitionBlobServiceClientFactory implements IBlobServiceClientFactory {

  @Autowired
  private PartitionServiceClient partitionService;


  @Override
  public BlobServiceClient getBlobServiceClient(String dataPartitionId) {
    Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");


    PartitionInfoAzure pi = this.partitionService.getPartition(dataPartitionId);

    StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(
        pi.getStorageAccountName(), pi.getStorageAccountKey());
    String endpoint = String
            
            
            
        .format(BLOB_STORAGE_ACCOUNT_BASE_URI_REGEX, AZURE_PROTOCOL, pi.getStorageAccountName());

    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(endpoint)
        .credential(storageSharedKeyCredential).buildClient();


    return blobServiceClient;
  }

  @Override
  public BlobServiceClient getSystemBlobServiceClient() {
    return null;
  }
}
