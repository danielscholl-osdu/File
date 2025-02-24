package org.opengroup.osdu.file.model.filemetadata.filedetails;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ValueWithUnit {

    @NotNull(message = "unitKey can not be null")
    @NotEmpty
    private String unitKey;

    @NotNull(message = "value can not be null")
    private double value;
}
