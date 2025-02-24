package util;

import org.opengroup.osdu.file.util.CloudStorageUtil;

public class StorageUtilAzure extends CloudStorageUtil {

  private static String storageAccount = AzureBlobService.getStorageAccount();

  private AzureBlobService blobService;


  public StorageUtilAzure() {
    AzureBlobServiceConfig config = new AzureBlobServiceConfig(storageAccount);
    blobService = config.azureBlobService();
  }


  @Override
  public void deleteCloudFile(String bucketName, String fileName) {
    blobService.deleteObject(bucketName,fileName);
  }
}
