package org.opengroup.osdu.file.model.filemetadata.filedetails;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;

import org.opengroup.osdu.file.model.filemetadata.MetaItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContentsDetails {

    private String kind;

    @JsonProperty("TargetKind")
    private String targetKind;

    @JsonProperty("FileType")
    private String fileType;

    @Valid
    @JsonProperty("FrameOfReference")
    private List<MetaItem> frameOfReference = new ArrayList<>();

    @JsonProperty("ExtensionProperties")
    private Object extensionProperties;

    @JsonProperty("ParentReference")
    private String parentReference;
}
