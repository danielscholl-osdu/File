package org.opengroup.osdu.file.provider.interfaces;

public interface IFileCollectionStorageUtilService {

  /**
   * Get cloud storage persistent location directory path
   * @param relativePath relative file path with in persistent location
   * @param partitionId data partition
   * @return
   */
  default String getPersistentLocation(String relativePath, String partitionId) {
    return null;
  }

  /**
   * Get cloud storage staging location directory path
   * @param relativePath relative file path with in staging location
   * @param partitionId data partition
   * @return
   */
  default String getStagingLocation(String relativePath, String partitionId) {
    return null;
  }
}
