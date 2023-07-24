/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "The file data container containing all necessary details of the file record")
public class FileData {

    @Schema(description = "An optional name of the dataset, e.g. a user friendly file or file collection name.")
    @JsonProperty("Name")
    private String name;

    @Schema(description = "An optional, textual description of the dataset.")
    @JsonProperty("Description")
    private String description;

    @Schema(description = "Total size of the dataset in bytes; for files it is the same as declared in FileSourceInfo.FileSize or the sum of all individual files. Implemented as string. The value must be convertible to a long integer (sizes can become very large).",
      pattern = "^[0-9]+$")
    @JsonProperty("TotalSize")
    private String totalSize;

    @Schema(description = "Encoding Format Type ID", pattern = "^srn:<namespace>:reference-data\\\\/EncodingFormatType:[^:]+:[0-9]*$")
    @JsonProperty("EncodingFormatTypeID")
    private String encodingFormatTypeID;

    @Schema(description = "Schema Format Type ID", pattern = "^srn:<namespace>:reference-data\\\\/SchemaFormatType:[^:]+:[0-9]*$")
    @JsonProperty("SchemaFormatTypeID")
    private String schemaFormatTypeID;

    @Schema(description = "Resource Home Region ID")
    @JsonProperty("ResourceHomeRegionID")
    private String resourceHomeRegionID;

    @ArraySchema(arraySchema = @Schema(implementation = String.class, description = "Resource Host Region IDs"))
    @JsonProperty("ResourceHostRegionIDs")
    private String[] resourceHostRegionIDs;

    @Schema(description = "Resource Curation Status")
    @JsonProperty("ResourceCurationStatus")
    private String resourceCurationStatus;

    @Schema(description = "Resource Lifecycle Status")
    @JsonProperty("ResourceLifecycleStatus")
    private String resourceLifecycleStatus;

    @Schema(description = "Resource Security Classification")
    @JsonProperty("ResourceSecurityClassification")
    private String resourceSecurityClassification;

    @Schema(description = "Source")
    @JsonProperty("Source")
    private String source;

    @JsonProperty("DatasetProperties")
    @NotNull(message = "DatasetProperties cannot be empty")
    @Valid
    private DatasetProperties datasetProperties;

    @Schema(description = "Existence Kind")
    @JsonProperty("ExistenceKind")
    private String existenceKind;

    @JsonProperty("Endian")
    private Endian endian;

    @Schema(description = "MD5 checksum of file bytes - a 32 byte hexadecimal number", pattern = "^[0-9a-fA-F]32}$")
    @JsonProperty("Checksum")
    private String checksum;

    @Schema(description = "File DMS Extension Properties")
    @JsonProperty("ExtensionProperties")
    private Map<String,Object> extensionProperties;

}
