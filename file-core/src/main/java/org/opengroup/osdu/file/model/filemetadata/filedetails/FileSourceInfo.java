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


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotEmpty;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "File Source Info")
public class FileSourceInfo {

    @Schema(description = "Relative file path for the data in the file")
    @JsonProperty("FileSource")
    @NotEmpty(message = "FileSource can not be empty")
    private String fileSource;

    @Schema(description = "File system path to the data file as it existed before loading to the data platform")
    @JsonProperty("PreloadFilePath")
    private String preloadFilePath;

    @Schema(description = "Optional user name or reference, who created the file prior to up-loading to the platform.")
    @JsonProperty("PreloadFileCreateUser")
    private String preloadFileCreateUser;

    @Schema(description = "Optional create date and time of the file prior to uploading to the platform.")
    @JsonProperty("PreloadFileCreateDate")
    private String preloadFileCreateDate;

    @Schema(description = "Optional user name or reference, who last modified the file prior to up-loading to the platform.")
    @JsonProperty("PreloadFileModifyUser")
    private String preloadFileModifyUser;

    @Schema(description = "Optional last modified date and time of the file prior to up-loading to the platform.")
    @JsonProperty("PreloadFileModifyDate")
    private String preloadFileModifyDate;

    @Schema(description = "user-friendly file name.")
    @JsonProperty("Name")
    private String name;

    @Schema(description = "Length of file in bytes. Implemented as string. The value must be convertible to a long integer (sizes can become very large).")
    @JsonProperty("FileSize")
    private String fileSize;

    @Schema(description = "Encoding Format Type ID", pattern = "^srn:<namespace>:reference-data\\\\/EncodingFormatType:[^:]+:[0-9]*$")
    @JsonProperty("EncodingFormatTypeID")
    private String encodingFormatTypeID;

    @Schema(description = "MD5 checksum of file bytes - a 32 byte hexadecimal number", pattern = "^[0-9a-fA-F]32}$")
    @JsonProperty("Checksum")
    private String checksum;

    @Schema(description = "The name of the checksum algorithm e.g. MD5, SHA-256.")
    @JsonProperty("ChecksumAlgorithm")
    private String checksumAlgorithm;
}
