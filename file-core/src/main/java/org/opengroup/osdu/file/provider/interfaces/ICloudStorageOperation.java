/*
 * Copyright 2021 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.interfaces;

import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.model.filecollection.FileCollectionOperationResponse;

import java.util.List;

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
   * Copies given list of files from the provided source location to the destination location.
   * @param fileCopyOperationList List of files to be copied
   * @return Copy Operation result along with complete path of copied file.
   */
  default List<FileCopyOperationResponse> copyFiles(List<FileCopyOperation> fileCopyOperationList) {
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

  /**
   * Copies given list of directories from the provided source location to the destination location.
   * @param fileCopyOperationList List of files to be copied
   * @return Copy Operation result along with complete path of copied file.
   */
  default List<FileCollectionOperationResponse> copyDirectory(List<FileCopyOperation> fileCopyOperationList) {
    return null;
  }
}
