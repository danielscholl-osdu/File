/*
 *  Copyright 2020-2022 Google LLC
 *  Copyright 2020-2022 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.inject.Inject;
import io.cucumber.java8.En;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.stepdefs.model.FileScope;
import org.opengroup.osdu.file.stepdefs.model.HttpRequest;
import org.opengroup.osdu.file.stepdefs.model.HttpResponse;
import org.opengroup.osdu.file.util.HttpClientFactory;
import org.opengroup.osdu.file.util.VersionInfoUtils;

public class InfoStepDef_GET implements En {

  @Inject
  private FileScope context;

  private VersionInfoUtils versionInfoUtil = new VersionInfoUtils();

  public InfoStepDef_GET() {

    Given("I send get request to version info endpoint", () -> {
      HttpRequest httpRequest = HttpRequest.builder()
          .url(TestConstants.HOST + "/v2/info")
          .httpMethod(HttpRequest.GET)
          .build();
      HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
      this.context.setHttpResponse(response);
    });

    Then("service should respond back with version info in response", () -> {
      assertEquals(200, this.context.getHttpResponse().getCode());

      VersionInfoUtils.VersionInfo responseObject = this.versionInfoUtil
          .getVersionInfoFromResponse(this.context.getHttpResponse());

      assertNotNull(responseObject.groupId);
      assertNotNull(responseObject.artifactId);
      assertNotNull(responseObject.version);
      assertNotNull(responseObject.buildTime);
      assertNotNull(responseObject.branch);
      assertNotNull(responseObject.commitId);
      assertNotNull(responseObject.commitMessage);
    });
  }
}
