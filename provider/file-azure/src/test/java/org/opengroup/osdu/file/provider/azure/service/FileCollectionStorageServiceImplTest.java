/*
 * Copyright 2020 Microsoft Corporation
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
package org.opengroup.osdu.file.provider.azure.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.datalakestorage.DataLakeStore;
import org.opengroup.osdu.azure.di.MSIConfiguration;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.DataLakeConfig;
import org.opengroup.osdu.file.provider.azure.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileCollectionStorageServiceImplTest {

  private static final String OSDU_USER = "osdu-user";
  @Mock
  DpsHeaders dpsHeaders;

  @Mock
  DataLakeStore dataLakeStore;

  @Mock
  ExpiryTimeUtil expiryTimeUtil;

  @Mock
  @Qualifier("DataLake")
  IStorageRepository storageRepository;

  @Mock
  FileLocationProperties fileLocationProperties;

  @Mock
  ServiceHelper serviceHelper;

  @Mock
  DataLakeConfig dataLakeConfig;

  @Captor
  ArgumentCaptor<String> directoryNameCaptor;

  @Mock
  MSIConfiguration msiConfiguration;

  @InjectMocks
  FileCollectionStorageServiceImpl fileCollectionStorageServiceImpl;

  @Test
  public void shouldCreateStorageInstructions() {
    SignedObject signedObject = getSignedObject();
    prepareMockForSignedUrl(signedObject);

    StorageInstructionsResponse response = fileCollectionStorageServiceImpl.
        createStorageInstructions(TestUtils.DIRECTORY_NAME, TestUtils.PARTITION);

    then(response.getProviderKey()).isEqualTo(TestUtils.PROVIDER_KEY);
    then(response.getStorageLocation().get("signedUrl")).isEqualTo(signedObject.getUrl().toString());

    verifyMockForSignedUrl();
  }

  @Test
  public void shouldThrowExceptionWhenResultFilepathIsMoreThan1024Characters() {
    String directoryId = RandomStringUtils.randomAlphanumeric(1024);

    Throwable thrown = catchThrowable(() -> fileCollectionStorageServiceImpl.
        createStorageInstructions(directoryId, TestUtils.PARTITION));
    then(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("The maximum directoryName length is 1024 characters");
    verify(storageRepository, never()).createSignedObject(anyString(), anyString());
  }

  @Test
  public void shouldCreateRetrievalInstructions() {
    URL mockSignedUrl = TestUtils.getAzureObjectUrl(TestUtils.STAGING_FILE_SYSTEM_NAME, TestUtils.DIRECTORY_NAME);
    prepareCreateSignedUrlFileLocationMocks(mockSignedUrl);
    when(msiConfiguration.getIsEnabled()).thenReturn(false);

    RetrievalInstructionsResponse response = fileCollectionStorageServiceImpl.
        createRetrievalInstructions(getFileRetrievalDataList());

    then(response.getDatasets().get(0).getProviderKey()).isEqualTo(TestUtils.PROVIDER_KEY);
    then(response.getDatasets().get(0).getDatasetRegistryId()).isEqualTo(TestUtils.FILE_COLLECTION_RECORD_ID);
    verifyCreateSignedUrlFileLocationMocks();
  }

  @Test
  public void testCreateRetrievalInstructions_EmptyUnsignedUrl_ThrowsIllegalArgumentException() {

    Throwable thrown = catchThrowable(() -> fileCollectionStorageServiceImpl.
        createRetrievalInstructions(getFileRetrievalDataList_EmptyUnsignedUrl()));
    then(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invalid received for unsignedURL");
  }

  @Test
  public void testCreateRetrievalInstructions_EmptySignedUrl_ThrowsAppException() throws MalformedURLException {
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(any())).thenReturn(offsetDateTime);
    when(serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH))
        .thenReturn(TestUtils.STAGING_FILE_SYSTEM_NAME);
    when(serviceHelper.getRelativeDirectoryPathFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH))
        .thenReturn(TestUtils.DIRECTORY_NAME);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(dataLakeStore.generatePreSignedURL(eq(TestUtils.PARTITION), eq(TestUtils.STAGING_FILE_SYSTEM_NAME),
        eq(TestUtils.DIRECTORY_NAME), eq(offsetDateTime), any())).thenReturn(TestUtils.EMPTY_STRING);

    Throwable thrown = catchThrowable(() -> fileCollectionStorageServiceImpl.
        createRetrievalInstructions(getFileRetrievalDataList()));
    then(thrown)
        .isInstanceOf(AppException.class)
        .hasMessageContaining("Could not generate signed URL for directory location");

    verify(expiryTimeUtil).getExpiryTimeInOffsetDateTime(any());
    verify(serviceHelper).getFileSystemNameFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH);
    verify(serviceHelper).getRelativeDirectoryPathFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH);
    verify(dpsHeaders).getPartitionId();
    verify(dataLakeStore).generatePreSignedURL(eq(TestUtils.PARTITION), eq(TestUtils.STAGING_FILE_SYSTEM_NAME),
        eq(TestUtils.DIRECTORY_NAME), eq(offsetDateTime), any());
  }

  private void prepareMockForSignedUrl(SignedObject signedObject) {
    when(dataLakeConfig.getStagingFileSystem()).thenReturn(TestUtils.STAGING_FILE_SYSTEM_NAME);
    when(fileLocationProperties.getUserId()).thenReturn(OSDU_USER);
    when(storageRepository.createSignedObject(eq(TestUtils.STAGING_FILE_SYSTEM_NAME), anyString()))
        .thenReturn(signedObject);
  }

  private void verifyMockForSignedUrl() {
    verify(dataLakeConfig).getStagingFileSystem();
    verify(fileLocationProperties).getUserId();
    verify(storageRepository).createSignedObject(eq(TestUtils.STAGING_FILE_SYSTEM_NAME), anyString());
  }

  private void prepareCreateSignedUrlFileLocationMocks(URL mockSignedUrl) {
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    when(fileLocationProperties.getUserId()).thenReturn(OSDU_USER);
    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(any())).thenReturn(offsetDateTime);
    when(serviceHelper.getFileSystemNameFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH))
        .thenReturn(TestUtils.STAGING_FILE_SYSTEM_NAME);
    when(serviceHelper.getRelativeDirectoryPathFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH))
        .thenReturn(TestUtils.DIRECTORY_NAME);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(dataLakeStore.generatePreSignedURL(eq(TestUtils.PARTITION), eq(TestUtils.STAGING_FILE_SYSTEM_NAME),
        eq(TestUtils.DIRECTORY_NAME), eq(offsetDateTime), any())).thenReturn(mockSignedUrl.toString());
  }

  private void verifyCreateSignedUrlFileLocationMocks() {
    verify(fileLocationProperties).getUserId();
    verify(expiryTimeUtil).getExpiryTimeInOffsetDateTime(any());
    verify(serviceHelper).getFileSystemNameFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH);
    verify(serviceHelper).getRelativeDirectoryPathFromAbsoluteDirectoryPath(TestUtils.ABSOLUTE_DIRECTORY_PATH);
    verify(dpsHeaders).getPartitionId();
    verify(dataLakeStore).generatePreSignedURL(eq(TestUtils.PARTITION), eq(TestUtils.STAGING_FILE_SYSTEM_NAME),
        eq(TestUtils.DIRECTORY_NAME), any(), any());
  }

  private SignedObject getSignedObject() {
    String fileSystemName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = TestUtils.USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);

    URI uri = TestUtils.getAzureObjectUri(fileSystemName, folderName);
    URL url = TestUtils.getAzureObjectUrl(fileSystemName, folderName);

    return SignedObject.builder()
        .uri(uri)
        .url(url)
        .build();
  }

  private Instant now() {
    return Instant.now(Clock.systemUTC());
  }

  private List<FileRetrievalData> getFileRetrievalDataList() {
    List<FileRetrievalData> fileRetrievalDataList = new ArrayList<>();
    fileRetrievalDataList.add(getFileRetrievalData());
    return fileRetrievalDataList;
  }

  private List<FileRetrievalData> getFileRetrievalDataList_EmptyUnsignedUrl() {
    List<FileRetrievalData> fileRetrievalDataList = new ArrayList<>();
    FileRetrievalData fileRetrievalData = FileRetrievalData.builder()
        .recordId(TestUtils.FILE_COLLECTION_RECORD_ID)
        .build();
    fileRetrievalDataList.add(fileRetrievalData);
    return fileRetrievalDataList;
  }

  private FileRetrievalData getFileRetrievalData() {
    return  FileRetrievalData.builder()
        .recordId(TestUtils.FILE_COLLECTION_RECORD_ID)
        .unsignedUrl( TestUtils.ABSOLUTE_DIRECTORY_PATH)
        .build();
  }
}
