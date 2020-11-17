package org.opengroup.osdu.file.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadUrlResponse {

	@JsonProperty("SignedUrl")
	private String signedUrl;
}
