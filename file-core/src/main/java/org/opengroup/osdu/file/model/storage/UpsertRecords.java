package org.opengroup.osdu.file.model.storage;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpsertRecords {
    private Integer recordCount;
    private List<String> recordIds;
    private List<String> skippedRecordIds;
}
