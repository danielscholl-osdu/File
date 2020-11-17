package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.opengroup.osdu.file.model.filemetadata.relationships.Relationships;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileData {

    @JsonProperty("SchemaFormatTypeID")
    private String schemaFormatTypeID;

    @JsonProperty("PreLoadFilePath")
    private String preLoadFilePath;

    @JsonProperty("FileSource")
    @NotEmpty(message = "FileSource can not be empty")
    private String fileSource;

    @JsonProperty("FileSize")
    private Integer fileSize;

    @JsonProperty("Name")
    @NotEmpty(message = "Name can not be empty")
    private String name;


    @JsonProperty("EncodingFormatTypeID")
    private String encodingFormatTypeID;

    @JsonProperty("Endian")
    @Valid
    @NotNull
    private Endian endian;

    @JsonProperty("LossyCompressionIndicator")
    private Boolean lossyCompressionIndicator;

    @JsonProperty("CompressionMethodTypeID")
    private String compressionMethodTypeID;

    @JsonProperty("CompressionLevel")
    private Integer compressionLevel;

    @JsonProperty("Checksum")
    private String checksum;

    @JsonProperty("VectorHeaderMapping")
    private List<VectorHeaderMapping> vectorHeaderMappings;

    @JsonProperty("relationships")
    private Relationships relationships;

    @JsonProperty("ExtensionProperties")
    private ExtensionProperties extensionProperties;
}
