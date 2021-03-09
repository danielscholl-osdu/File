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

package org.opengroup.osdu.file.provider.aws.di.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class FileDeliveryItemAWSImpl implements FileDeliveryItem {

    @JsonProperty("signedUrl")
    URI signedUrl;

    @JsonProperty("signedUrlExpiration")
    Date signedUrlExpiration;

    @JsonProperty("unsignedUrl")
    String unsignedUrl;

    @JsonProperty("createdAt")
    Instant createdAt;

    @JsonProperty("fileName")
    String fileName;

    @JsonProperty("connectionString")
    String connectionString;

    @JsonProperty("credentials")
    private TemporaryCredentials credentials;

    @JsonProperty("region")
    private String region;

}
