package util;

import org.opengroup.osdu.file.util.CloudStorageUtil;

public class StorageUtilAzure extends CloudStorageUtil {
  private static String clientSecret = System.getProperty("TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("TESTER_SERVICEPRINCIPAL_SECRET"));
  private static String clientId = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
  private static String tenantId = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));

  private AzureBlobService blobService;

  public StorageUtilAzure() {
    AzureBlobServiceConfig config = new AzureBlobServiceConfig(storageAccount);
    blobService = config.azureBlobService();
  }
  private static String storageAccount = AzureBlobService.getStorageAccount();

  @Override
  public void deleteCloudFile(String bucketName, String fileName) {

  }
}
