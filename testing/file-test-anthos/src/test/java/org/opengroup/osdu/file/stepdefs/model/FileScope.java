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

package org.opengroup.osdu.file.stepdefs.model;

import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import java.util.Map;
import lombok.Data;
import org.opengroup.osdu.file.util.FileUtils;

@ScenarioScoped
@Data
public class FileScope {
	@Inject
	private FileUtils fileUtils;

	private String token;
	private Map<String, String> authHeaders;
	private Map<String, String> queryParams;
	private HttpResponse httpResponse;
	private String id;
	private String fileSource;
	private String signedUrl;
	private String version;

	private String jobId;
	private String status;
	private String responseCode;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
