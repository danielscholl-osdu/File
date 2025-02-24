package org.opengroup.osdu.file.provider.azure.repository;

import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.Context;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.azure.di.MSIConfiguration;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedObject;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.config.BlobStoreConfig;
import org.opengroup.osdu.file.provider.azure.model.blob.Blob;
import org.opengroup.osdu.file.provider.azure.model.blob.BlobInfo;
import org.opengroup.osdu.file.provider.azure.storage.Storage;
import org.opengroup.osdu.file.util.ExpiryTimeUtil;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageRepositoryTest {

  @Mock
  private Storage storage;

  @Mock
  BlobStore blobStore;

  @Mock
  BlobStoreConfig blobStoreConfig;

  @Mock
  private BlobServiceClientWrapper blobServiceClientWrapper;

  @Mock
  private MSIConfiguration msiConfiguration;

  @Mock
  DpsHeaders dpsHeaders;

  @Mock
  private ExpiryTimeUtil expiryTimeUtil;

  @Mock
  private StorageAccountsClient storageAccountsClient;

  @InjectMocks
  StorageRepository storageRepository;

  @Test
  public void shouldCreateSignedObject_MsiEnabled() {
    prepareMock(true);
    String signedUrl = TestUtils.getSignedURL(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
    OffsetDateTime expiryTimeInOffsetDateTime = mock(OffsetDateTime.class);

    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime()))
        .thenReturn(expiryTimeInOffsetDateTime);
    when(blobStore.generatePreSignedUrlWithUserDelegationSas(eq(TestUtils.PARTITION), eq(TestUtils.CONTAINER_NAME),
        eq(TestUtils.FILE_PATH), any(OffsetDateTime.class), any(BlobSasPermission.class))).thenReturn(signedUrl);

    SignedObject signedObject = storageRepository.createSignedObject(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);

    then(signedObject).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.AZURE_URL_CONDITION);
      then(url.getUrl().toString()).isEqualTo(signedUrl);
    });

    verify(msiConfiguration).getIsEnabled();
    verify(blobServiceClientWrapper).getStorageAccount();
    verify(dpsHeaders, times(2)).getPartitionId();
    verify(blobStore).generatePreSignedUrlWithUserDelegationSas(eq(TestUtils.PARTITION), eq(TestUtils.CONTAINER_NAME),
        eq(TestUtils.FILE_PATH), any(OffsetDateTime.class), any(BlobSasPermission.class));
  }

  @Test
  public void shouldCreateSignedObject_MsiNotEnabled() {
    prepareMock(false);
    String signedUrl = TestUtils.getSignedURL(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters();
    OffsetDateTime expiryTimeInOffsetDateTime = mock(OffsetDateTime.class);

    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime()))
        .thenReturn(expiryTimeInOffsetDateTime);
    when(blobStore.generatePreSignedURL(eq(TestUtils.PARTITION),
        eq(TestUtils.FILE_PATH), eq(TestUtils.CONTAINER_NAME), any(OffsetDateTime.class), any(BlobSasPermission.class))).thenReturn(signedUrl);

    SignedObject signedObject = storageRepository.createSignedObject(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);

    then(signedObject).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.AZURE_URL_CONDITION);
      then(url.getUrl().toString()).isEqualTo(signedUrl);
    });

    verify(msiConfiguration).getIsEnabled();
    verify(blobServiceClientWrapper).getStorageAccount();
    verify(dpsHeaders, times(2)).getPartitionId();
    verify(blobStore).generatePreSignedURL(eq(TestUtils.PARTITION),
        eq(TestUtils.FILE_PATH), eq(TestUtils.CONTAINER_NAME), any(OffsetDateTime.class), any(BlobSasPermission.class));
  }

  @Test
  public void shouldCreateSignedObjectWithSignedUrlParametersAndMsiEnabled() {
    prepareMock(true);
    String signedUrl = TestUtils.getSignedURL(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters("2D");
    OffsetDateTime expiryTimeInOffsetDateTime = OffsetDateTime.now().plusDays(2);

    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime()))
        .thenReturn(expiryTimeInOffsetDateTime);
    when(blobStore.generatePreSignedUrlWithUserDelegationSas(eq(TestUtils.PARTITION), eq(TestUtils.CONTAINER_NAME),
        eq(TestUtils.FILE_PATH), any(OffsetDateTime.class), any(BlobSasPermission.class))).thenReturn(signedUrl);

    SignedObject signedObject = storageRepository.getSignedObjectBasedOnParams(TestUtils.CONTAINER_NAME,
        TestUtils.FILE_PATH, signedUrlParameters);

    then(signedObject).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.AZURE_URL_CONDITION);
      then(url.getUrl().toString()).isEqualTo(signedUrl);
    });

    verify(msiConfiguration).getIsEnabled();
    verify(blobServiceClientWrapper).getStorageAccount();
    verify(dpsHeaders, times(2)).getPartitionId();
    verify(expiryTimeUtil, times(1)).getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime());
    verify(blobStore).generatePreSignedUrlWithUserDelegationSas(eq(TestUtils.PARTITION), eq(TestUtils.CONTAINER_NAME),
        eq(TestUtils.FILE_PATH), eq(expiryTimeInOffsetDateTime), any(BlobSasPermission.class));
  }
  @Test
  public void shouldCreateSignedObjectWithSignedUrlParametersAndMsiDisabled() {
    prepareMock(false);
    String signedUrl = TestUtils.getSignedURL(TestUtils.CONTAINER_NAME, TestUtils.FILE_PATH);
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters("2D");
    OffsetDateTime expiryTimeInOffsetDateTime = OffsetDateTime.now().plusDays(2);

    when(expiryTimeUtil.getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime()))
        .thenReturn(expiryTimeInOffsetDateTime);
    when(blobStore.generatePreSignedURL(eq(TestUtils.PARTITION),
        eq(TestUtils.FILE_PATH), eq(TestUtils.CONTAINER_NAME), any(OffsetDateTime.class), any(BlobSasPermission.class))).thenReturn(signedUrl);

    SignedObject signedObject = storageRepository.getSignedObjectBasedOnParams(TestUtils.CONTAINER_NAME,
        TestUtils.FILE_PATH, signedUrlParameters);

    then(signedObject).satisfies(url -> {
      then(url.getUrl().toString()).is(TestUtils.AZURE_URL_CONDITION);
      then(url.getUrl().toString()).isEqualTo(signedUrl);
    });

    verify(msiConfiguration).getIsEnabled();
    verify(blobServiceClientWrapper).getStorageAccount();
    verify(dpsHeaders, times(2)).getPartitionId();
    verify(expiryTimeUtil, times(1)).getExpiryTimeInOffsetDateTime(signedUrlParameters.getExpiryTime());
    verify(blobStore).generatePreSignedURL(eq(TestUtils.PARTITION),
        eq(TestUtils.FILE_PATH), eq(TestUtils.CONTAINER_NAME), eq(expiryTimeInOffsetDateTime), any(BlobSasPermission.class));
  }

  @Test
  public void shouldRevokeUserDelegationKeysSuccessfully() {
    Map<String, String> revokeURLRequest = new HashMap<>();
    revokeURLRequest.put("resourceGroup", "testresourcegroup");
    revokeURLRequest.put("storageAccount", "teststorageaccount");
    SimpleResponse<Void> simpleResponse = new SimpleResponse<>(null, 200, null, null);
    when(storageAccountsClient
        .revokeUserDelegationKeysWithResponse("testresourcegroup", "teststorageaccount", Context.NONE))
        .thenReturn(simpleResponse);

    assertTrue(storageRepository.revokeUserDelegationKeys(revokeURLRequest));

    verify(storageAccountsClient,times(1))
        .revokeUserDelegationKeysWithResponse("testresourcegroup", "teststorageaccount", Context.NONE);
  }

  @Test
  public void revokeUserDelegationKeys_shouldThrowAppException() {
    Map<String, String> revokeURLRequest = new HashMap<>();
    revokeURLRequest.put("resourceGroup", "testresourcegroup");
    revokeURLRequest.put("storageAccount", "teststorageaccount");
    when(storageAccountsClient
        .revokeUserDelegationKeysWithResponse("testresourcegroup", "teststorageaccount", Context.NONE))
        .thenThrow(ManagementException.class);

    assertThrows(AppException.class, ()-> storageRepository.revokeUserDelegationKeys(revokeURLRequest));

    verify(storageAccountsClient,times(1))
        .revokeUserDelegationKeysWithResponse("testresourcegroup", "teststorageaccount", Context.NONE);
  }

  private void prepareMock(boolean isMsiEnabled) {
    Blob blob = mock(Blob.class);
    when(msiConfiguration.getIsEnabled()).thenReturn(isMsiEnabled);
    when(blob.getName()).thenReturn(TestUtils.FILE_PATH);
    when(blob.getContainer()).thenReturn(TestUtils.CONTAINER_NAME);
    when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    when(dpsHeaders.getPartitionId()).thenReturn(TestUtils.PARTITION);
    when(storage.create(eq(TestUtils.PARTITION), any(BlobInfo.class), eq(ArrayUtils.EMPTY_BYTE_ARRAY)))
        .thenReturn(blob);
  }
}
