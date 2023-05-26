package org.opengroup.osdu.file.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileAdminServiceTest {

  @InjectMocks
  FileAdminService fileAdminService;

  @Mock
  IStorageService storageService;

  @Mock
  JaxRsDpsLog log;

  @Test
  public void revokeURL_Success() {
    Map<String, String> revokeURLRequest = new HashMap<>();
    revokeURLRequest.put("resourceGroup","test-resource-group-name");
    revokeURLRequest.put("storageAccount","test-storage-account-name");
    when(storageService.revokeUrl(revokeURLRequest)).thenReturn(true);

    fileAdminService.revokeUrl(revokeURLRequest);
    String expectedMessage = "Result of revoke urls is true";

    Mockito.verify(storageService, Mockito.times(1)).revokeUrl(revokeURLRequest);
    Mockito.verify(log, Mockito.times(1)).info(expectedMessage);
  }

}
