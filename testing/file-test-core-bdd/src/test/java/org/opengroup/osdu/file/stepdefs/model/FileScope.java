package org.opengroup.osdu.file.stepdefs.model;

import java.util.Map;

import org.opengroup.osdu.file.util.test.FileUtils;

import com.google.inject.Inject;

import io.cucumber.guice.ScenarioScoped;
import lombok.Data;

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
