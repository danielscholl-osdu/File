/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.ibm.apitest;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.file.apitest.Delivery;
import org.opengroup.osdu.file.ibm.util.HttpClientIBM;

public class TestDelivery extends Delivery {

  @BeforeAll
  public static void setUp() throws IOException {
    client = new HttpClientIBM();
  }

  
  @Test
  @Override
  @Disabled
  public void ingestRecordsForAGivenSchemaAndTestSearchAndDelivery() throws Exception {
	    
  }

  @Test
  @Override
  @Disabled
  public void ingestInvalidRecordsForAGivenSchemaAndGetBadResponse() throws Exception {
	    
  }

  @BeforeAll
  @Override
  public void tearDown() throws Exception {
//	  TODO:delete records created in couch as part of test case execution
//    if (!locationResponses.isEmpty()) {
//      for (LocationResponse response : locationResponses) {
//      }
//    }
  }
  

  @AfterAll
  @Override
  public void beforeAll() throws Exception {
    //TODO: remove this block
  }

}
