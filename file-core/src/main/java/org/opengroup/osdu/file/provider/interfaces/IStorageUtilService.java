package org.opengroup.osdu.file.provider.interfaces;

public interface IStorageUtilService {

  //stub implementation
  default String getPersistentLocation(String relativePath, String partitionId) {
    return null;
  }

  //stub implementation
  default String getStagingLocation(String relativePath, String partitionId) {
    return null;
  }

}
