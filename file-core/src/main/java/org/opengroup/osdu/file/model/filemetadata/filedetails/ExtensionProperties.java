package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExtensionProperties {

    @JsonProperty("Classification")
    private String classification;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("ExternalIds")
    private List<String> externalIds;

    @JsonProperty("FileDateCreated")
    private Object fileDateCreated;

    @JsonProperty("FileDateModified")
    private Object fileDateModified;

    @JsonProperty("FileContentsDetails")
    private Map<String,Object> fileContentDetails;


}
