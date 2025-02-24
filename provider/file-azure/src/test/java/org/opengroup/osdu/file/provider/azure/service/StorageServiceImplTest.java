/*
 * Copyright 2020  Microsoft Corporation
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

import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.storage.blob.sas.BlobSasPermission;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.di.MSIConfiguration;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.exception.OsduBadRequestException;
import org.opengroup.osdu.file.model.FileRetrievalData;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.model.property.FileLocationProperties;
import org.opengroup.osdu.file.provider.interfaces.IStorageRepository;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class StorageServiceImplTest {

  @Mock
  ServiceHelper serviceHelper;

  @Mock
  BlobStore blobStore;

  @Mock
  DpsHeaders dpsHeaders;

  @Mock
  private IStorageRepository storageRepository;

  @Mock
  private BlobStoreConfig blobStoreConfig;

  @Mock
  private MSIConfiguration msiConfiguration;

  private ExpiryTimeUtil expiryTimeUtil;

  @Captor
  ArgumentCaptor<String> filenameCaptor;

  private IStorageService storageService;

  @BeforeEach
  void setUp() {
    initMocks(this);
    FileLocationProperties fileLocationProperties
        = new FileLocationProperties(TestUtils.STAGING_CONTAINER_NAME, TestUtils.USER_DES_ID);

    expiryTimeUtil = new ExpiryTimeUtil();
    storageService = new StorageServiceImpl(blobStore, dpsHeaders, fileLocationProperties,
        storageRepository, blobStoreConfig, expiryTimeUtil, msiConfiguration, serviceHelper);

    lenient().when(msiConfiguration.getIsEnabled()).thenReturn(false);

  }

  @Test
  void shouldCreateObjectSignedUrl() {
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
    Mockito.when(blobStoreConfig.getStagingContainer()).thenReturn(TestUtils.STAGING_CONTAINER_NAME);

    // given
    SignedObject signedObject = getSignedObject();
    given(storageRepository.getSignedObjectBasedOnParams(eq(TestUtils.STAGING_CONTAINER_NAME), anyString(), eq(signedUrlParameters))).willReturn(signedObject);

    // when
    SignedUrl signedUrl = storageService.createSignedUrl(
        TestUtils.FILE_ID, TestUtils.AUTHORIZATION_TOKEN, TestUtils.PARTITION);

    // then
    then(signedUrl).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.AZURE_URL_CONDITION);
      then(url.getUri().toString()).matches(TestUtils.AZURE_OBJECT_URI);
      then(url.getCreatedAt()).isBeforeOrEqualTo(now());
      then(url.getCreatedBy()).isEqualTo(TestUtils.USER_DES_ID);
    });

    verify(storageRepository).getSignedObjectBasedOnParams(eq(TestUtils.STAGING_CONTAINER_NAME), filenameCaptor.capture(), eq(signedUrlParameters));
    then(filenameCaptor.getValue()).matches(".*?");
  }

  @Test
  void shouldThrowExceptionWhenResultFilepathIsMoreThan1024Characters() {
    // given
    String fileId = RandomStringUtils.randomAlphanumeric(1024);
    // when
    Throwable thrown = catchThrowable(() -> storageService.createSignedUrl(fileId,
        TestUtils.AUTHORIZATION_TOKEN, TestUtils.PARTITION));
    // then
    then(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("The maximum filepath length is 1024 characters");
    verify(storageRepository, never()).createSignedObject(anyString(), anyString());
  }

  @Test
  void createSignedUrlFileLocation_shouldThrow_whenUnsignedURL_OR_AuthorizationToken_IsBlank() {
    // given
    String[] invalidUnsignedURLs = {"", "    ", null};
    for(String unsignedURl: invalidUnsignedURLs) {
      // when

      Throwable thrown = catchThrowable(() -> storageService
          .createSignedUrlFileLocation(unsignedURl, TestUtils.AUTHORIZATION_TOKEN,
              new SignedUrlParameters()));
      // then
      then(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(String.format("invalid received for authorizationToken (value: %s) or unsignedURL (value: %s)",
                  TestUtils.AUTHORIZATION_TOKEN, unsignedURl));
      verify(blobStore, never()).generatePreSignedURL(
          anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class));
    }

    // given
    String[] invalidAuthTokens = {"", "    ", null};
    for(String authToken: invalidAuthTokens) {
      // when
      Throwable thrown = catchThrowable(() -> storageService
          .createSignedUrlFileLocation(TestUtils.ABSOLUTE_FILE_PATH, authToken,
              new SignedUrlParameters()));
      // then
      then(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(String.format("invalid received for authorizationToken (value: %s) or unsignedURL (value: %s)",
              authToken, TestUtils.ABSOLUTE_FILE_PATH));
      verify(blobStore, never()).generatePreSignedURL(
          anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class));
    }
  }

  @Test
  void createSignedUrlFileLocation_shouldThrow_whenSignedURLGeneratedIsNull() {
    // setup
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.doReturn(null).when(blobStore).generatePreSignedURL(
        anyString(),anyString(),anyString(),any(OffsetDateTime.class),any(BlobSasPermission.class));

    Throwable thrown = catchThrowable(() -> storageService
        .createSignedUrlFileLocation(TestUtils.ABSOLUTE_FILE_PATH, TestUtils.AUTHORIZATION_TOKEN,
            new SignedUrlParameters()));
    // then
    then(thrown)
        .isInstanceOf(InternalServerErrorException.class)
        .hasMessageContaining(String.format("Could not generate signed URL for file location %s", TestUtils.ABSOLUTE_FILE_PATH));
  }

  @Test
  void createSignedUrlFileLocation_ShouldCallGeneratePreSignedURL() {
    prepareMockForCreateSignedUrlFileLocation();
    storageService
        .createSignedUrlFileLocation(TestUtils.ABSOLUTE_FILE_PATH, TestUtils.AUTHORIZATION_TOKEN,
            new SignedUrlParameters());
    verify(blobStore,times(1)).generatePreSignedURL(
        anyString(),anyString(),anyString(),any(OffsetDateTime.class), any(BlobSasPermission.class));
  }

  @Test
  void createSignedUrlFileLocation_with_fileName_ShouldCallGeneratePreSignedURL() {
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String signedUrlString = getSignedObject().getUrl().toString();
    doReturn(signedUrlString).when(blobStore).generatePreSignedURL(
        anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class), anyString(), anyString());

    storageService.createSignedUrlFileLocation(TestUtils.ABSOLUTE_FILE_PATH,TestUtils.AUTHORIZATION_TOKEN,new SignedUrlParameters(null, TestUtils.FILE_NAME, TestUtils.FILE_CONTENT_TYPE));
    verify(blobStore,times(1)).generatePreSignedURL(
        anyString(),anyString(),anyString(),any(OffsetDateTime.class), any(BlobSasPermission.class), anyString(), anyString());
  }

  @Test
  void createSignedUrlFileLocation_fileName_contains_comma_ShouldCallGeneratePreSignedURL() {
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
            .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
            .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String signedUrlString = getSignedObject().getUrl().toString();
    doReturn(signedUrlString).when(blobStore).generatePreSignedURL(
        anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class), anyString(), anyString());

    storageService.createSignedUrlFileLocation(TestUtils.ABSOLUTE_FILE_PATH,TestUtils.AUTHORIZATION_TOKEN,new SignedUrlParameters(null, TestUtils.FILE_NAME_WITH_COMMA, TestUtils.FILE_CONTENT_TYPE));
    verify(blobStore,times(1)).generatePreSignedURL(
        anyString(),anyString(),anyString(),any(OffsetDateTime.class), any(BlobSasPermission.class), anyString(), anyString());
  }

  @Test
  void shouldCreateStorageInstructions() {
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
    Mockito.when(blobStoreConfig.getStagingContainer()).thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    when(dpsHeaders.getAuthorization()).thenReturn(TestUtils.AUTHORIZATION_TOKEN);
    // given
    SignedObject signedObject = getSignedObject();
    given(storageRepository.getSignedObjectBasedOnParams(eq(TestUtils.STAGING_CONTAINER_NAME), anyString(),
        eq(signedUrlParameters))).willReturn(signedObject);

    // when
    StorageInstructionsResponse storageInstructions = storageService.createStorageInstructions(
        TestUtils.FILE_ID, TestUtils.PARTITION);

    // then
    then(storageInstructions).satisfies(response -> {
      then(response.getProviderKey()).isEqualTo(TestUtils.PROVIDER_KEY);
      then(response.getStorageLocation()).isNotEmpty();
    });

    verify(storageRepository).getSignedObjectBasedOnParams(eq(TestUtils.STAGING_CONTAINER_NAME), filenameCaptor.capture(),
        eq(signedUrlParameters));
    verify(blobStoreConfig).getStagingContainer();
    verify(dpsHeaders).getAuthorization();
    then(filenameCaptor.getValue()).matches(".*?");
  }

  @Test
  void shouldRetrievalStorageInstructions() {
    List<FileRetrievalData> fileRetrievalDataList = getFileRetrievalDataList();
    prepareMockForCreateSignedUrlFileLocation();
    Mockito.when(dpsHeaders.getAuthorization()).thenReturn(TestUtils.AUTHORIZATION_TOKEN);

    RetrievalInstructionsResponse response = storageService.createRetrievalInstructions(fileRetrievalDataList);

    List<DatasetRetrievalProperties> datasetRetrievalProperties = response.getDatasets();
    Assertions.assertNotNull(datasetRetrievalProperties.get(0).getRetrievalProperties());
    Assertions.assertEquals(datasetRetrievalProperties.get(0).getDatasetRegistryId(),
        fileRetrievalDataList.get(0).getRecordId());

    verify(blobStore,times(1)).generatePreSignedURL(
        anyString(),anyString(),anyString(),any(OffsetDateTime.class), any(BlobSasPermission.class));
    verify(dpsHeaders).getAuthorization();
    verifyMockForCreateSignedUrlFileLocation();
  }

  @Test
  public void shouldRevokeURL() {
    Map<String, String> revokeURLRequest = createRevokeURLRequest("test-resource-group-name", "test-storage-account-name");
    when(storageRepository.revokeUserDelegationKeys(revokeURLRequest)).thenReturn(true);

    assertTrue(storageService.revokeUrl(revokeURLRequest));
    verify(storageRepository, times(1)).revokeUserDelegationKeys(revokeURLRequest);
  }

  @Test
  public void revokeURLShouldThrowBadRequestExceptionWhenResourceGroupIsEmpty() {
    Map<String, String> revokeURLRequest = createRevokeURLRequest("", "test-storage-account-name");
    String expectedMessage = "Illegal argument for resourceGroup {  } or storageAccount { test-storage-account-name }";
    // when
    Throwable thrown = catchThrowable(() -> storageService.revokeUrl(revokeURLRequest));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessageContaining(expectedMessage);
    verify(storageRepository, never()).revokeUserDelegationKeys(revokeURLRequest);
  }

  @Test
  public void revokeURLShouldThrowBadRequestExceptionWhenResourceGroupIsNull() {
    Map<String, String> revokeURLRequest = createRevokeURLRequest(null, "test-storage-account-name");
    String expectedMessage = "Illegal argument for resourceGroup { null } or storageAccount { test-storage-account-name }";
    // when
    Throwable thrown = catchThrowable(() -> storageService.revokeUrl(revokeURLRequest));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessageContaining(expectedMessage);
    verify(storageRepository, never()).revokeUserDelegationKeys(revokeURLRequest);
  }
  @Test
  public void revokeURLShouldThrowBadRequestExceptionWhenStorageAccountIsEmpty() {
    Map<String, String> revokeURLRequest = createRevokeURLRequest("test-resource-group-name", "");
    String expectedMessage = "Illegal argument for resourceGroup { test-resource-group-name } or storageAccount {  }";
    // when
    Throwable thrown = catchThrowable(() -> storageService.revokeUrl(revokeURLRequest));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessageContaining(expectedMessage);
    verify(storageRepository, never()).revokeUserDelegationKeys(revokeURLRequest);
  }

  @Test
  public void revokeURLShouldThrowBadRequestExceptionWhenStorageAccountIsNull() {
    Map<String, String> revokeURLRequest = createRevokeURLRequest("test-resource-group-name", null);
    String expectedMessage = "Illegal argument for resourceGroup { test-resource-group-name } or storageAccount { null }";
    // when
    Throwable thrown = catchThrowable(() -> storageService.revokeUrl(revokeURLRequest));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessageContaining(expectedMessage);
    verify(storageRepository, never()).revokeUserDelegationKeys(revokeURLRequest);
  }

  private void prepareMockForCreateSignedUrlFileLocation() {
    Mockito.when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    Mockito.when(serviceHelper
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.STAGING_CONTAINER_NAME);
    Mockito.when(serviceHelper
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH))
        .thenReturn(TestUtils.RELATIVE_FILE_PATH);
    String signedUrlString = getSignedObject().getUrl().toString();
    doReturn(signedUrlString).when(blobStore).generatePreSignedURL(
        anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class));
  }

  private void verifyMockForCreateSignedUrlFileLocation() {
    Mockito.verify(dpsHeaders).getPartitionId();
    Mockito.verify(serviceHelper)
        .getContainerNameFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH);
    Mockito.verify(serviceHelper)
        .getRelativeFilePathFromAbsoluteFilePath(TestUtils.ABSOLUTE_FILE_PATH);
    verify(blobStore).generatePreSignedURL(
        anyString(), anyString(), anyString(), any(OffsetDateTime.class), any(BlobSasPermission.class));
  }

  private List<FileRetrievalData> getFileRetrievalDataList() {
    List<FileRetrievalData> fileRetrievalDataList = new ArrayList<>();
    FileRetrievalData fileRetrievalData  = FileRetrievalData.builder()
        .recordId(TestUtils.FILE_RECORD_ID)
        .unsignedUrl(TestUtils.ABSOLUTE_FILE_PATH)
        .build();
    fileRetrievalDataList.add(fileRetrievalData);
    return fileRetrievalDataList;
  }

  private SignedObject getSignedObject() {
    String containerName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = TestUtils.USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URI uri = TestUtils.getAzureObjectUri(containerName, folderName, filename);
    URL url = TestUtils.getAzureObjectUrl(containerName, folderName, filename);

    return SignedObject.builder()
        .uri(uri)
        .url(url)
        .build();
  }

  @NotNull
  private Map<String, String> createRevokeURLRequest(String resourceGroup, String storageAccount) {
    Map<String, String> revokeURLRequest = new HashMap<>();
    revokeURLRequest.put("resourceGroup", resourceGroup);
    revokeURLRequest.put("storageAccount", storageAccount);
    return revokeURLRequest;
  }

  private Instant now() {
    return Instant.now(Clock.systemUTC());
  }

}
