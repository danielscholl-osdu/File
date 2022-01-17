/*
 * Copyright 2020  Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.azure.config;

import com.azure.storage.file.datalake.DataLakeServiceClient;
import org.opengroup.osdu.azure.datalakestorage.IDataLakeClientFactory;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;

@Component
@RequestScope
public class DataLakeClientWrapper {

  private final String storageAccount;

  @Autowired
  private IDataLakeClientFactory dataLakeClientFactory;

  @Inject
  public DataLakeClientWrapper(DpsHeaders headers, IDataLakeClientFactory dataLakeClientFactory) {
    this.dataLakeClientFactory = dataLakeClientFactory;
    String dataPartitionId = headers.getPartitionId();
    Validators.checkNotNullAndNotEmpty(dataPartitionId, "dataPartitionId");

     DataLakeServiceClient dataLakeServiceClient = this.dataLakeClientFactory.getDataLakeServiceClient(dataPartitionId);
    storageAccount = dataLakeServiceClient.getAccountName();
  }

  public String getStorageAccount() {
    return storageAccount;
  }
}
