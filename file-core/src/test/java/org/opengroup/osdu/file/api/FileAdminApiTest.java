package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.file.service.FileAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
public class FileAdminApiTest {

  @Mock
  private FileAdminService fileAdminService;

  @InjectMocks
  FileAdminApi fileAdminApi;


  @Test
  public void test_revokeURL() {
    Map<String, String> revokeURLRequest = new HashMap<>();
    revokeURLRequest.put("resourceGroup","test-resource-group-name");
    revokeURLRequest.put("storageAccount","test-storage-account-name");

    doNothing().when(fileAdminService).revokeUrl(revokeURLRequest);

    assertEquals(HttpStatus.NO_CONTENT, fileAdminApi.revokeURL(revokeURLRequest).getStatusCode());
  }

}
