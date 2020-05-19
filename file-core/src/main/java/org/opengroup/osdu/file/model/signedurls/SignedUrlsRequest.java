package org.opengroup.osdu.file.model.signedurls;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignedUrlsRequest {
    @JsonProperty("UnsignedUrls")
    List<String> unsignedUrls;
}
