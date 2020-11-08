package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.file.exception.OsduBadRequestException;

public interface ICloudStorageOperation {

  /**
   * Copy file from a source location in cloud storage to destination location
   * @param sourceFilePath path of the source file
   * @param destinationFilePath path of destination file
   * @return complete path of copied file
   * @throws OsduBadRequestException if source or destination file path is invalid
   */
  default String copyFile(String sourceFilePath, String destinationFilePath) throws OsduBadRequestException {
    return null;
  }

  /**
   * delete a file form cloud storage
   * @param filePath path of the that needs to be deleted
   * @return true if file is deleted successfully
   */
  default Boolean deleteFile(String filePath) {
    return false;
  }

}
