// Copyright © 2021 Amazon Web Services
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

import java.util.ArrayList;

import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.file.provider.aws.di.model.GetCreateUpdateDatasetRegistryResponse;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetStorageInstructionsResponse;

public interface IDatasetService {

    GetCreateUpdateDatasetRegistryResponse getDatasetRegistry(String datasetRegistryId) throws DatasetException;

    GetCreateUpdateDatasetRegistryResponse getDatasetRegistry(ArrayList<String> datasetRegistryIds) throws DatasetException;

    GetCreateUpdateDatasetRegistryResponse registerDataset(Record datasetRecord) throws DatasetException;

    GetCreateUpdateDatasetRegistryResponse registerDataset(ArrayList<Record> datasetRecords) throws DatasetException;

    GetDatasetStorageInstructionsResponse getStorageInstructions(String kindSubType) throws DatasetException;

    GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(String datasetRegistryId) throws DatasetException;

    GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(ArrayList<String> datasetRegistryIds) throws DatasetException;

}
