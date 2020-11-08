package org.opengroup.osdu.file.util;

import java.util.UUID;

import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataUtil {

  String getUniqueId() {
    return UUID.randomUUID().toString();
  }

  public String generateRecordId(String dataPartitionId) {
    return new StringBuilder(dataPartitionId)
        .append(FileMetadataConstant.KIND_SEPRATOR)
        .append(FileMetadataConstant.FILE)
        .append(FileMetadataConstant.KIND_SEPRATOR)
        .append(getUniqueId())
        .toString();
  }

}
