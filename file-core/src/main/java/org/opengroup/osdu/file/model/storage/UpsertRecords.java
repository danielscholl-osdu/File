package org.opengroup.osdu.file.model.storage;

import java.util.List;

import lombok.Data;

@Data
public class UpsertRecords {
    private Integer recordCount;
    private List<String> recordIds;
    private List<String> skippedRecordIds;
}
