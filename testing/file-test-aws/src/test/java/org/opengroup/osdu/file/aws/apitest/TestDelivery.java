/**
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

package org.opengroup.osdu.file.aws.apitest;

import java.io.IOException;
import java.util.Map;

import com.sun.jersey.api.client.ClientResponse;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.commons.util.StringUtils;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.apitest.Delivery;
import org.opengroup.osdu.file.aws.util.CloudStorageUtilAws;
import org.opengroup.osdu.file.aws.util.HttpClientAws;
import org.opengroup.osdu.file.models.delivery.DeliveryTestIndex;
import org.opengroup.osdu.file.models.delivery.SrnFileData;
import org.opengroup.osdu.file.models.delivery.UrlSigningResponse;
import org.opengroup.osdu.file.util.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDelivery extends Delivery {

    // protected static final DummyRecordsHelper RECORDS_HELPER = new DummyRecordsHelper();
    private static String containerName = System.getProperty("STAGING_CONTAINER_NAME", System.getenv("STAGING_CONTAINER_NAME"));

    public TestDelivery() {
        client = new HttpClientAws();
        cloudStorageUtil = new CloudStorageUtilAws();
    }

    @Override
    public void validate_cloud_provider_connection_string(UrlSigningResponse signedResponse) {
        Map<String, SrnFileData> items = signedResponse.getProcessed();
        for (Map.Entry<String,SrnFileData> item : items.entrySet()) {
            SrnFileData entry = item.getValue();
            String connectionString = entry.getConnectionString();
            if ( ! (
                    connectionString.contains("AccessKeyId") &&
                            connectionString.contains("SecretAccessKey") &&
                            connectionString.contains("SessionToken") &&
                            connectionString.contains("Expiration")
                    )) {
                fail("GetFileSignedUrl response either doesn't contain a " +
                        "ConnectionString property or it is malformed : " + connectionString);
            }
        }
    }
}
