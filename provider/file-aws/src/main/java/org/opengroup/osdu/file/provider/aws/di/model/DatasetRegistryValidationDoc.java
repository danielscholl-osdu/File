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

package org.opengroup.osdu.file.provider.aws.di.model;

public class DatasetRegistryValidationDoc {
    
  private DatasetRegistryValidationDoc() {
    // private constructor
  }

  public static final String MISSING_DATASET_PROPERTIES_VALIDATION = "DatasetProperties cannot be null";
  public static final String MISSING_DATASET_REGISTRIES_ARRAY = "datasetRegistries cannot be empty";
  public static final String MAX_DATASET_REGISTRIES_EXCEEDED = "Only 20 Dataset Registries can be ingested at a time";
  public static final String MISSING_DATASET_REGISTRY_SCHEMA_ERROR_FORMAT = "No schema for Dataset Registry was found: Expecting '%s'. It must be registered first.";
  public static final String DATASET_REGISTRY_MISSING_PROPERTY_VALIDATION_FORMAT = "Dataset Registry Schema Validation Failed: Expected property '%s' is missing";
    

}
