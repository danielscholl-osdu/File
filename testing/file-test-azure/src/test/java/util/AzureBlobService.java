// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package util;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AzureBlobService {

    private static String clientSecret = System.getProperty("TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("TESTER_SERVICEPRINCIPAL_SECRET"));
    private static String clientId = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
    private static String tenantId = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
    private static String storageAccount;

    AzureBlobService(String storageAccount) {
        this.storageAccount = storageAccount;
    }

    public static String getStorageAccount() {
        return System.getProperty("AZURE_STORAGE_ACCOUNT", System.getenv("AZURE_STORAGE_ACCOUNT"));
    }




    private BlobContainerClient getBlobContainerClient(String accountName, String containerName) {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientSecret(clientSecret)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(getBlobAccountUrl(accountName))
                .credential(clientSecretCredential)
                .containerName(containerName)
                .buildClient();
        return blobContainerClient;
    }

    private static String getBlobAccountUrl(String accountName) {
        return String.format("https://%s.blob.core.windows.net", accountName);
    }


    private static String generateBlobName(String blobName) {
        return blobName.replace(":","_");
    }



    private static String generateBlobPath(String accountName, String containerName, String blobName) {
        return String.format("https://%s.blob.core.windows.net/%s%s", accountName, containerName, blobName);
    }

    public void deleteObject(String containerName, String blobName) {
      String blobPath = generateBlobPath(storageAccount, containerName, generateBlobName(blobName));
      BlobUrlParts parts = BlobUrlParts.parse(blobPath);
      BlobContainerClient blobContainerClient = getBlobContainerClient(parts.getAccountName(), parts.getBlobContainerName());
      BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(parts.getBlobName()).getBlockBlobClient();
      if (blockBlobClient.exists()) {
        blockBlobClient.delete();
      }
    }




}
