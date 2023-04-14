package org.opengroup.osdu.file.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Download URL response")
public class DownloadUrlResponse {

  @Schema(description = "Signed URL")
	@JsonProperty("SignedUrl")
	private String signedUrl;
}
