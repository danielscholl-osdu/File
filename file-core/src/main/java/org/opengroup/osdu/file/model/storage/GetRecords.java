package org.opengroup.osdu.file.model.storage;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GetRecords {
    List<String> records = new ArrayList<>();
    List<String> attributes = new ArrayList<>();
}
