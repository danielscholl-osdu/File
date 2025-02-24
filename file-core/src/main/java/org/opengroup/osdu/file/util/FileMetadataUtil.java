package org.opengroup.osdu.file.util;

import java.util.UUID;

import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataUtil {

  String generateUniqueId() {
    return UUID.randomUUID().toString();
  }

  public String generateRecordId(String dataPartitionId, String fileEntity) {
    return new StringBuilder(dataPartitionId)
        .append(FileMetadataConstant.KIND_SEPRATOR)
        .append(fileEntity)
        .append(FileMetadataConstant.KIND_SEPRATOR)
        .append(generateUniqueId())
        .toString();
  }

}
