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

public class AzureBlobService {



    private static String storageAccount;

    AzureBlobService(String storageAccount) {
        this.storageAccount = storageAccount;
    }

    public static String getStorageAccount() {
        return System.getProperty("AZURE_STORAGE_ACCOUNT", System.getenv("AZURE_STORAGE_ACCOUNT"));
    }

    private static String generateContainerPath(String accountName, String containerName) {
        return String.format("https://%s.blob.core.windows.net/%s", accountName, containerName);
    }


}
