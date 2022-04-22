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

package org.opengroup.osdu.file.provider.aws.di;

import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

public class DatasetFactory implements IDatasetFactory {

    private final DatasetAPIConfig config;
    private final HttpResponseBodyMapper bodyMapper;

    public DatasetFactory(DatasetAPIConfig config, HttpResponseBodyMapper bodyMapper) {
        if (config == null) {
            throw new IllegalArgumentException("StorageAPIConfig cannot be empty");
        }
        this.config = config;
        this.bodyMapper = bodyMapper;
    }

    @Override
    public IDatasetService create(DpsHeaders headers) {
        if (headers == null) {
            throw new NullPointerException("headers cannot be null");
        }

        if (bodyMapper == null) {
            throw new NullPointerException("bodyMapper cannot be null");
        }

        return new DatasetService(this.config, new HttpClient(), headers, bodyMapper);
    }
}
