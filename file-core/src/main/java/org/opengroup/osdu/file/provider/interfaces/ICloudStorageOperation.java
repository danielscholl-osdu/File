package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.file.exception.OsduBadRequestException;

public interface ICloudStorageOperation {

  default String copyFile(String fromFile, String toFile) throws OsduBadRequestException {
    return null;
  }

  default Boolean deleteFile(String stagingLocation) {
    return false;
  }

}
