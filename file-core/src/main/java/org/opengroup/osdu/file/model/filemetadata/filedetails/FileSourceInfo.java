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

package org.opengroup.osdu.file.model.filemetadata.filedetails;


import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileSourceInfo {

    @JsonProperty("FileSource")
    @NotEmpty(message = "FileSource can not be empty")
    private String fileSource;

    @JsonProperty("PreLoadFilePath")
    private String preLoadFilePath;

    @JsonProperty("PreloadFileCreateUser")
    private String preloadFileCreateUser;

    @JsonProperty("PreloadFileCreateDate")
    private String preloadFileCreateDate;

    @JsonProperty("PreloadFileModifyUser")
    private String preloadFileModifyUser;

    @JsonProperty("PreloadFileModifyDate")
    private String preloadFileModifyDate;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("FileSize")
    private String fileSize;

    @JsonProperty("EncodingFormatTypeID")
    private String encodingFormatTypeID;

    @JsonProperty("Checksum")
    private String checksum;

    @JsonProperty("ChecksumAlgorithm")
    private String checksumAlgorithm;
}
