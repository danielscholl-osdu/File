package org.opengroup.osdu.file.model.storage;

import java.util.List;

import lombok.Data;

@Data
public class Ancestry {
    private List<String> parents;
}
