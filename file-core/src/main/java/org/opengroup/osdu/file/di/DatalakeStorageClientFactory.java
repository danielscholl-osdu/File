// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.file.di;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.StorageAPIConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatalakeStorageClientFactory extends AbstractFactoryBean<DataLakeStorageFactory> {

    @Value("${storage.api}")
    private String api;

    @Value("${authorize.api.key}")
    private String apiKey;

	  private final HttpResponseBodyMapper bodyMapper ;

    @Override
    protected DataLakeStorageFactory createInstance() throws Exception {

        return new DataLakeStorageFactory(
            StorageAPIConfig.builder()
                .storageServiceBaseUrl(this.api)
                .apiKey(this.apiKey).build(),
                bodyMapper);
    }

    @Override
    public Class<?> getObjectType() {
        return DataLakeStorageFactory.class;
    }
}
