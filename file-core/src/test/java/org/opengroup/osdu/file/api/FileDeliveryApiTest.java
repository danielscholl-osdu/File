package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.file.model.DownloadUrlParameters;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.service.FileDeliveryService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    DownloadUrlParameters downloadUrlParameters = new DownloadUrlParameters("7D");
    when(fileDeliveryService.getSignedUrlsByRecordId("1234",downloadUrlParameters)).thenReturn(downloadUrlResponse);
    assertEquals(HttpStatus.OK, fileDeliveryApi.downloadURL("1234","7D").getStatusCode());
  }

}
