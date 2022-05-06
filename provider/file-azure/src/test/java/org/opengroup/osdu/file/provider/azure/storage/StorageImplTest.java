package org.opengroup.osdu.file.provider.azure.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.blobstorage.IBlobContainerClientFactory;
import org.opengroup.osdu.file.provider.azure.TestUtils;
import org.opengroup.osdu.file.provider.azure.config.AzureBootstrapConfig;
import org.opengroup.osdu.file.provider.azure.config.BlobServiceClientWrapper;
import org.opengroup.osdu.file.provider.azure.model.blob.Blob;
import org.opengroup.osdu.file.provider.azure.service.AzureTokenServiceImpl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageImplTest {

  @Mock
  private AzureBootstrapConfig azureBootstrapConfig;

  @Mock
  private BlobServiceClientWrapper blobServiceClientWrapper;

  @Mock
  private IBlobContainerClientFactory blobContainerClientFactory;

  @Mock
  AzureTokenServiceImpl azureTokenService;

  @InjectMocks
  StorageImpl storageImpl;

  @Test
  public void shouldCreateBlob() {
    prepareMocks(false, false);

    byte[] content = "content".getBytes();
    Blob blob = storageImpl.create(TestUtils.PARTITION, TestUtils.getBlobInfo(), content);

    Assertions.assertEquals(blob.getBlobId().getContainer(), TestUtils.getBlobId().getContainer());
    Assertions.assertEquals(blob.getBlobId().getName(), TestUtils.getBlobId().getName());
  }

  @Test
  public void shouldCreateBlob_BlockBlobClientAlreadyExists() {
    prepareMocks(true, true);

    byte[] content = "content".getBytes();
    Blob blob = storageImpl.create(TestUtils.PARTITION, TestUtils.getBlobInfo(), content);

    Assertions.assertEquals(blob.getBlobId().getContainer(), TestUtils.getBlobId().getContainer());
    Assertions.assertEquals(blob.getBlobId().getName(), TestUtils.getBlobId().getName());
  }

  @Test
  public void shouldCreateSignUrl() {
    TimeUnit mockTimeUnit = mock(TimeUnit.class);
    long duration = 1000;
    when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    when(azureTokenService.sign(anyString(), eq(duration), eq(mockTimeUnit)))
        .thenReturn(TestUtils.getSignedURL(TestUtils.STAGING_CONTAINER_NAME, TestUtils.FILE_NAME));

    URL signedUrl = storageImpl.signUrl(TestUtils.getBlobInfo(), duration, mockTimeUnit);
    Assertions.assertEquals(signedUrl.toString(), TestUtils.getSignedURL(TestUtils.STAGING_CONTAINER_NAME, TestUtils.FILE_NAME));

    verify(blobServiceClientWrapper).getStorageAccount();
    verify(azureTokenService).sign(anyString(), eq(duration), eq(mockTimeUnit));
  }

  @Test
  public void shouldCreateSignUrl_ThrowMalformedURLException() {
    TimeUnit mockTimeUnit = mock(TimeUnit.class);
    long duration = 1000;
    when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    when(azureTokenService.sign(anyString(), eq(duration), eq(mockTimeUnit)))
        .thenReturn(TestUtils.INVALID_URL);

    Assertions.assertThrows(MalformedURLException.class , ()->{storageImpl.signUrl(TestUtils.getBlobInfo(), duration, mockTimeUnit);});

    verify(blobServiceClientWrapper).getStorageAccount();
    verify(azureTokenService).sign(anyString(), eq(duration), eq(mockTimeUnit));
  }

  private void prepareMocks(boolean isBlobContainerClientExists, boolean isBlockBlobClientExists) {
    BlobContainerClient mockBlobContainerClient = mock(BlobContainerClient.class);
    BlockBlobClient mockBlockBlobClient = mock(BlockBlobClient.class);
    BlobClient mockBlobClient = mock(BlobClient.class);
    lenient().when(blobServiceClientWrapper.getStorageAccount()).thenReturn(TestUtils.STORAGE_NAME);
    lenient().when(blobContainerClientFactory.getClient(
        TestUtils.PARTITION, TestUtils.STAGING_CONTAINER_NAME)).thenReturn(mockBlobContainerClient);
    lenient().when(mockBlobContainerClient.exists()).thenReturn(isBlobContainerClientExists);
    lenient().doNothing().when(mockBlobContainerClient).create();
    lenient().when(mockBlobContainerClient.getBlobClient(TestUtils.BLOB_NAME)).thenReturn(mockBlobClient);
    lenient().when(mockBlobClient.getBlockBlobClient()).thenReturn(mockBlockBlobClient);
    lenient().when(mockBlockBlobClient.exists()).thenReturn(isBlockBlobClientExists);
    lenient().when(mockBlockBlobClient.upload(any(InputStream.class), any(Long.class))).thenReturn(mock(BlockBlobItem.class));
  }
}
