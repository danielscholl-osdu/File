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

package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.opengroup.osdu.file.model.filemetadata.relationships.Relationships;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileData {

    @JsonProperty("Name")
    @NotEmpty(message = "Name can not be empty")
    private String name;

    @JsonProperty("Description")    
    private String description;

    @JsonProperty("TotalSize")
    private String totalSize;

    @JsonProperty("EncodingFormatTypeID")
    private String encodingFormatTypeID;

    @JsonProperty("SchemaFormatTypeID")
    private String schemaFormatTypeID;

    @JsonProperty("ResourceHomeRegionID")
    private String resourceHomeRegionID;

    @JsonProperty("ResourceHostRegionIDs")
    private String[] resourceHostRegionIDs;

    @JsonProperty("ResourceCurationStatus")
    private String resourceCurationStatus;

    @JsonProperty("ResourceLifecycleStatus")
    private String resourceLifecycleStatus;

    @JsonProperty("ResourceSecurityClassification")
    private String resourceSecurityClassification;

    @JsonProperty("Source")
    private String source;

    @JsonProperty("DatasetProperties")
    @NotNull(message = "DatasetProperties cannot be empty")
    @Valid
    private DatasetProperties datasetProperties;
    
    @JsonProperty("ExistenceKind")
    private String existenceKind;

    @JsonProperty("Endian")
    @Valid
    @NotNull
    private Endian endian; 

    @JsonProperty("Checksum")
    private String checksum;

    @JsonProperty("ExtensionProperties")
    private ExtensionProperties extensionProperties;
}
