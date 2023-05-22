package org.opengroup.osdu.file.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.logging.AuditLogger;
import org.opengroup.osdu.file.model.SignedUrlParameters;
import org.opengroup.osdu.file.provider.interfaces.ILocationService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileLocationApiTest {

  private final String response = "response";

  @Mock
  private DpsHeaders headers;

  @Mock
  private ILocationService locationService;

  @Mock
  private AuditLogger auditLogger;

  @Mock
  private LocationRequest locationRequest;

  @Mock
  private LocationResponse locationResponse;

  @Mock
  private FileLocationResponse fileLocationResponse;

  @Mock
  private FileLocationRequest fileLocationRequest;

  @InjectMocks
  FileLocationApi fileLocationApi;


  @BeforeEach
  public void init() {

  }

  @Test
  public void test_getLocation() {

    when(locationService.getLocation(locationRequest, headers)).thenReturn(locationResponse);
    when(locationResponse.toString()).thenReturn(response);
    LocationResponse result = fileLocationApi.getLocation(locationRequest);

    assertNotNull(result);
    verify(locationService, times(1)).getLocation(locationRequest, headers);
    verify(auditLogger, times(1)).createLocationSuccess(eq(Collections.singletonList(response)));
  }

  @Test
  public void test_getFileLocation() {

    when(locationService.getFileLocation(fileLocationRequest, headers)).thenReturn(fileLocationResponse);
    when(fileLocationResponse.toString()).thenReturn(response);
    FileLocationResponse result = fileLocationApi.getFileLocation(fileLocationRequest);

    assertNotNull(result);
    verify(locationService, times(1)).getFileLocation(fileLocationRequest, headers);
    verify(auditLogger, times(1)).readFileLocationSuccess(eq(Collections.singletonList(response)));
  }

  @Test
  public void test_getLocationFile() throws JsonProcessingException {
    SignedUrlParameters signedUrlParameters = new SignedUrlParameters("7D");
    when(locationService.getLocation(any(LocationRequest.class), eq(headers), eq(signedUrlParameters))).thenReturn(locationResponse);

    LocationResponse result = fileLocationApi.getLocationFile("7D");

    assertNotNull(result);
    verify(locationService, times(1)).getLocation(any(LocationRequest.class), eq(headers), eq(signedUrlParameters));
  }


}
