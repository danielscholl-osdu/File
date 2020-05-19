package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;

import java.util.Map;

public interface IRecordService {
  public Map<String, Object> createOrUpdateRecord(Record record, DpsHeaders headers);
}
