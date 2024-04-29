package org.opengroup.osdu.file.model.filemetadata;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;

import org.opengroup.osdu.file.model.filemetadata.filedetails.ForKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaItem {

    @NotNull(message = "frameOfReference.kind must not be null")
    private ForKind kind;

    private String name;

    @NotNull(message = "persistableReference must not be null")
    private String persistableReference;

    private List<String> propertyNames = new ArrayList<>();

    private List<String> propertyValues = new ArrayList<>();

    private int uncertainty;
}
