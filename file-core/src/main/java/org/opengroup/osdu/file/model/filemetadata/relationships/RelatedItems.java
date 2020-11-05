package org.opengroup.osdu.file.model.filemetadata.relationships;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedItems {

    private List<Integer> confidences = new ArrayList<>();

    private List<String> ids = new LinkedList<>();

    private List<String> names = new ArrayList<>();

    private List<Integer> versions = new ArrayList<>();
}
