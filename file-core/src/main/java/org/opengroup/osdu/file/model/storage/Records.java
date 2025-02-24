package org.opengroup.osdu.file.model.storage;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Records {
    private List<Record> records = new ArrayList<>();
    private List<String> invalidRecords = new ArrayList<>();
    private List<String> retryRecords = new ArrayList<>();
}
