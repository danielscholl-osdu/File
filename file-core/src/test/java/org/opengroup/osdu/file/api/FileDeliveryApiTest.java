package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.service.FileDeliveryService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileDeliveryApiTest {

  @Mock
  private FileDeliveryService fileDeliveryService;

  @InjectMocks
  FileDeliveryApi fileDeliveryApi;


  @Test
  public void test_downloadURL() throws StorageException {
    DownloadUrlResponse downloadUrlResponse = new DownloadUrlResponse();
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters("7D");
    when(fileDeliveryService.getSignedUrlsByRecordId("1234", signedUrlParameters)).thenReturn(downloadUrlResponse);
    assertEquals(HttpStatus.OK, fileDeliveryApi.downloadURL("1234","7D").getStatusCode());
  }

}
