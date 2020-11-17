package org.opengroup.osdu.file.model.filemetadata.relationships;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentEntity {

    private int confidence;

    private String id;

    private String name;

    private int version;
}
