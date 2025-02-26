package org.opengroup.osdu.file.model.filemetadata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "File metadata Response")
public class FileMetadataResponse {

    @Schema(description = "Unique identifier generated by the system for the file metadata record.")
    private String id;
}
