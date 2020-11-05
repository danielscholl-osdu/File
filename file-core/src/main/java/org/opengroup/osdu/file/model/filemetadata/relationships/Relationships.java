package org.opengroup.osdu.file.model.filemetadata.relationships;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Relationships {

    private ParentEntity parentEntity;

    private RelatedItems relatedItems;
}
