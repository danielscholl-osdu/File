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

package org.opengroup.osdu.file.service.delivery;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.opengroup.osdu.core.common.model.search.QueryResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(SpringExtension.class)
public class UnsignedUrlLocationMapperDefaultTest {

    @InjectMocks
    private DeliveryUnsignedUrlLocationMapperDefault CUT;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void getUnsignedURLFromSearchResponse()
    {
        // Arrange
        String unsignedUrl = "s3://aws-osdu-demo-r2/data/provided/markers_csv/3676.csv";
        String responseBody = "{\"results\":[{\"data\":{\"ResourceID\":\"srn:file/csv:7344999246049527:\"," +
                "\"Data.GroupTypeProperties.PreloadFilePath\":\"" + unsignedUrl + "\"," +
                "\"Data.GroupTypeProperties.FileSource\":\"\",\"ResourceTypeID\":\"srn:type:file/csv:\"," +
                "\"ResourceSecurityClassification\":\"srn:reference-data/ResourceSecurityClassification:RESTRICTED:\"," +
                "\"AssociativeID\":\"f-1\"},\"kind\":\"opendes:osdu:file:0.0.4\",\"namespace\":\"opendes:osdu\",\"legal\":" +
                "{\"legaltags\":[\"opendes-public-usa-dataset-1\"],\"otherRelevantDataCountries\":[\"US\"],\"status\":\"compliant\"}," +
                "\"id\":\"opendes:doc:543b05af7d094739a7e6ad1496cbf5ec\",\"acl\":{\"viewers\":[\"data.default.viewers@opendes.testing.com\"]," +
                "\"owners\":[\"data.default.owners@opendes.testing.com\"]},\"type\":\"file\",\"version\":1585246320682077}," +
                "],\"aggregations\":null,\"totalCount\":1}";

        QueryResponse queryResponse = this.gson.fromJson(responseBody, QueryResponse.class);
        Map<String, Object> searchResponse = queryResponse.getResults().get(0);

        // Act
        String actual = CUT.getUnsignedURLFromSearchResponse(searchResponse);

        // Assert
        assertEquals(unsignedUrl, actual);
    }

    @Test
    public void getUnsignedURLFromSearchResponse_CastFailed_returnsNull()
    {
        // Arrange
        int value = 123;
        Map<String, Object> searchResponse = new HashMap<>();
        searchResponse.put("data", value);

        // Act
        String actual = CUT.getUnsignedURLFromSearchResponse(searchResponse);

        // Assert
        assertEquals(null, actual);
    }
}
