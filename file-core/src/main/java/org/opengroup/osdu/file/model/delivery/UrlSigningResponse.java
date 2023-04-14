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

package org.opengroup.osdu.file.model.delivery;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents a model for Url Signing Response",
    example = "{ \"unprocessed\": [ \"srn:some-invalid-srn\" ], \"processed\": { \"srn:some-valid-srn\":  { \"signedUrl\": \"https://...\" }}}")
public class UrlSigningResponse {

    @ArraySchema(arraySchema = @Schema(implementation = String.class, description = "A list of SRNs which could not be processed"))
    @JsonProperty("unprocessed")
    List<String> unprocessed;

    @Schema(implementation = Map.class, description = "A list of SRNs which are processed")
    @JsonProperty("processed")
    Map<String, SrnFileData> processed;

}
