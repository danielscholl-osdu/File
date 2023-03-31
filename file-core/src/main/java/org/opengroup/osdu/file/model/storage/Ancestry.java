package org.opengroup.osdu.file.model.storage;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "A named list of entities in the data lake as a dictionary item.")
public class Ancestry {

    @ArraySchema(arraySchema = @Schema(implementation = String.class, description = "An array of one or more entity references in the data lake."))
    private List<String> parents;
}
