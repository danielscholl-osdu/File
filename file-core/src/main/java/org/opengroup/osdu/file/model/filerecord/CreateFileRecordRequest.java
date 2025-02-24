package org.opengroup.osdu.file.model.filerecord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFileRecordRequest {
  // Will we need a legal tag and acl from the user? Or will these be inferred somehow
  // could infer acl to be data.file
  // could infer legal tag to a default US legal tag
    @JsonProperty("UnsignedUrl")
    String unsignedUrl;

    @JsonProperty("FileName")
    String fileName;

    @JsonProperty("FileDescription")
    String fileDescription;
}
