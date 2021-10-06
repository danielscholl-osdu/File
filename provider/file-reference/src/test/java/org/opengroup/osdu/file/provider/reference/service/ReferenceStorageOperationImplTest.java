/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.file.provider.reference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.minio.ObjectWriteResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.file.FileCopyOperation;
import org.opengroup.osdu.file.model.file.FileCopyOperationResponse;
import org.opengroup.osdu.file.provider.reference.repository.MinioRepository;
import org.opengroup.osdu.file.provider.reference.util.MinioPathUtil;

@ExtendWith(MockitoExtension.class)
class ReferenceStorageOperationImplTest {

  private static final String SOURCE = "http://osdu/staging-area/folder-id/file-id";
  private static final String DESTINATION = "http://osdu/persistent-area/folder-id/file-id";
  private static final String ANOTHER_SOURCE = "http://osdu/staging-area/folder-id/file-id-2";
  private static final String ANOTHER_DESTINATION = "http://osdu/persistent-area/folder-id/file-id-2";
  private static final String SOURCE_BUCKET = "staging-area";
  private static final String DESTINATION_BUCKET = "persistent-area";
  private static final String SOURCE_FILE = "folder-id/file-id";
  private static final String DESTINATION_FILE = "folder-id/file-id";
  private static final String SOURCE_FOLDER = "folder-id";

  @InjectMocks
  ReferenceStorageOperationImpl storageOperation;

  @Mock
  MinioPathUtil minioPathUtil;
  @Mock
  MinioRepository minioRepository;
  @Mock
  ObjectWriteResponse writeResponse;

  @Test
  void testCopy_Success() {
    when(minioPathUtil.getBucketName(SOURCE)).thenReturn(SOURCE_BUCKET);
    when(minioPathUtil.getDirectoryPath(SOURCE)).thenReturn(SOURCE_FILE);
    when(minioPathUtil.getBucketName(DESTINATION)).thenReturn(DESTINATION_BUCKET);
    when(minioPathUtil.getDirectoryPath(DESTINATION)).thenReturn(DESTINATION_FILE);
    when(minioRepository.copyFile(any())).thenReturn(writeResponse);
    when(minioPathUtil.getCompleteFilePath(any(), any())).thenReturn(DESTINATION);
    String completeFilePath = storageOperation.copyFile(SOURCE, DESTINATION);
    assertEquals(DESTINATION, completeFilePath);
  }

  @Test
  void testCopy_InvalidResourcePath() {
    assertThrows(
        OsduBadRequestException.class, () -> storageOperation.copyFile(SOURCE, DESTINATION));
  }

  @Test
  void testCopyFiles_Success() {
    FileCopyOperation firstOperation = FileCopyOperation.builder()
        .sourcePath(SOURCE)
        .destinationPath(DESTINATION)
        .build();
    FileCopyOperation secondOperation = FileCopyOperation.builder()
        .sourcePath(ANOTHER_SOURCE)
        .destinationPath(ANOTHER_DESTINATION)
        .build();
    List<FileCopyOperation> fileCopyOperationList = Stream.of(firstOperation, secondOperation)
            .collect(Collectors.toList());
    List<FileCopyOperationResponse> responses = storageOperation.copyFiles(fileCopyOperationList);
  }

  @Test
  void testDeleteFile_Success() {
    when(minioPathUtil.getBucketName(SOURCE)).thenReturn(SOURCE_BUCKET);
    when(minioPathUtil.getDirectoryPath(SOURCE)).thenReturn(SOURCE_FILE);
    when(minioPathUtil.getFolderName(SOURCE_FILE)).thenReturn(SOURCE_FOLDER);
    Boolean isFileDeleted = storageOperation.deleteFile(SOURCE);
    assertTrue(isFileDeleted);
  }
}