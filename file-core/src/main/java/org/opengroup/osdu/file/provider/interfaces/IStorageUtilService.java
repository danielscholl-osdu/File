package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.file.constant.ChecksumAlgorithm;

public interface IStorageUtilService {

  /**
   * Get cloud storage persistent location file path
   * @param relativePath relative file path with in persistent location
   * @param partitionId data partition
   * @return
   */
  default String getPersistentLocation(String relativePath, String partitionId) {
    return null;
  }

  /**
   * Get cloud storage staging location file path
   * @param relativePath relative file path with in staging location
   * @param partitionId data partition
   * @return
   */
  default String getStagingLocation(String relativePath, String partitionId) {
    return null;
  }

  /**
   * Method is used to get the checksum of a file specified at file path.
   * @param filePath Path of file to get the metadata
   * @return File checksum.
   */
  default String getChecksum(final String filePath) { return null; }

  /**
   * Method is used to get the checksum algorithm.
   * @return File checksum algorithm.
   */
  default ChecksumAlgorithm getChecksumAlgorithm() { return ChecksumAlgorithm.NONE; }
}
