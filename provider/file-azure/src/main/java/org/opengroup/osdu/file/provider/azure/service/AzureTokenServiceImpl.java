/*
 * Copyright © Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.azure.service;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.java.Log;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.concurrent.TimeUnit;

@Log
@Component
public class AzureTokenServiceImpl {

  @Autowired
  private BlobServiceClientWrapper blobServiceClientWrapper;

  private static final DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

  private UserDelegationKey getUserDelegationKey() {
    String storageAccount = this.blobServiceClientWrapper.getStorageAccount();
    String endpoint = calcBlobAccountUrl(storageAccount);
    BlobServiceClient rbacKeySource = new BlobServiceClientBuilder()
        .endpoint(endpoint)
        .credential(defaultCredential)
        .buildClient();
    OffsetDateTime expires = calcTokenExpirationDate(1, TimeUnit.DAYS);
    return rbacKeySource.getUserDelegationKey(null, expires);
  }

  public String sign(String blobUrl, long duration, TimeUnit timeUnit) {
    UserDelegationKey key = getUserDelegationKey();
    BlobClient tokenSource = new BlobClientBuilder()
        .credential(defaultCredential)
        .endpoint(blobUrl)
        .buildClient();
    BlobSasPermission permissions = BlobSasPermission.parse("rw");
    if (duration < 0)
      duration = 7L;
    if(timeUnit == null)
      timeUnit = TimeUnit.DAYS;
    OffsetDateTime expires = calcTokenExpirationDate(duration, timeUnit);
    BlobServiceSasSignatureValues tokenProps = new BlobServiceSasSignatureValues(expires, permissions);
    String sasToken = tokenSource.generateUserDelegationSas(tokenProps, key);
    String sasUri = String.format("%s?%s", blobUrl, sasToken);
    return sasUri;
  }

  private static String calcBlobAccountUrl(String accountName) {
    return String.format("https://%s.blob.core.windows.net", accountName);
  }

  private static OffsetDateTime calcTokenExpirationDate(long duration, TimeUnit timeUnit) {
    if (timeUnit == null) {
      throw new NullPointerException("Time unit cannot be null");
    }
    if (timeUnit == TimeUnit.DAYS) {
      return OffsetDateTime.now(ZoneOffset.UTC).plusDays(duration);
    } else if (timeUnit == TimeUnit.SECONDS) {
      return OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(duration);
    } else if (timeUnit == TimeUnit.NANOSECONDS) {
      return OffsetDateTime.now(ZoneOffset.UTC).plusNanos(duration);
    } else if (timeUnit == TimeUnit.MINUTES) {
      return OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(duration);
    } else if (timeUnit == TimeUnit.HOURS) {
      return OffsetDateTime.now(ZoneOffset.UTC).plusHours(duration);
    } else {
      throw new UnsupportedTemporalTypeException("Unsupported temporal type");
    }
  }
}
