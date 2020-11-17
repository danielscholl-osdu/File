package org.opengroup.osdu.file.model.filemetadata.filedetails;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorHeaderMapping {

    @JsonProperty("KeyName")
    private String keyName;

    @JsonProperty("WordFormat")
    private String wordFormat;

    @JsonProperty("WordWidth")
    private Integer wordWidth;

    @JsonProperty("Position")
    private Integer position;

    @JsonProperty("UoM")
    private String uom;

    @JsonProperty("ScalarIndicator")
    @Valid
    private ScalarIndicator scalarIndicator;

    @JsonProperty("ScalarOverride")
    private Integer scalarOverride;


}
