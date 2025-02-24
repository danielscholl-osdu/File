package org.opengroup.osdu.file.exception.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.file.exception.*;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class RestExceptionHandlerTest {

  @InjectMocks
  private RestExceptionHandler handler;

  @Mock
  private WebRequest mockRequest;

  @Mock
  HttpHeaders headers;

  HttpStatus status;

  @Mock
  private HttpInputMessage mockInputMessage;

  @Mock
  private HttpMessageNotReadableException mockHttpMsgException;

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  JaxRsDpsLog log;

  @BeforeEach
  void setUp() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    mockRequest = Mockito.mock(WebRequest.class);
    Mockito.when(mockRequest.getHeader("correlation-id")).thenReturn("sample-id");

  }

  @Test
  public void testNotFoundException() {
    NotFoundException ex = new NotFoundException();
    ResponseEntity<Object> response = handler.handleNotFoundException(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testBadRequestException() {
    OsduBadRequestException ex = new OsduBadRequestException("Bad request");
    ResponseEntity<Object> response = handler.handleBadRequest(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testAuthorizationException() {
    OsduUnauthorizedException ex = new OsduUnauthorizedException("Unauthorized");
    ResponseEntity<Object> response = handler.handleAccessDeniedException(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  public void testApplicationException() {
    ApplicationException ex = new ApplicationException();
    ResponseEntity<Object> response = handler.handleApplicationException(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void testRuntimeException() {
    RuntimeException ex = new RuntimeException();
    ResponseEntity<Object> response = handler.handleRuntimeException(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void testHandleStorageException() {
    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setBody(
        "{\"code\":400,\"reason\":\"Invalid ACL\",\"message\":\"Acl not match with tenant or domain\"}");
    httpResponse.setResponseCode(400);
    StorageException ex = new StorageException("message", httpResponse);
    ResponseEntity<Object> response = handler.handleStorageException(ex, mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testHandleHttpMessageNotReadableExceptionEnum() {
    EnumValidationException ex = new EnumValidationException("key", "inputValue");
    Mockito.when(mockHttpMsgException.getMostSpecificCause()).thenReturn(ex);
    ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(mockHttpMsgException,
                                                                           headers,
                                                                           status.BAD_REQUEST,
                                                                           mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testHandleHttpMessageNotReadableExceptionGeneral() {
    OsduBadRequestException ex = new OsduBadRequestException("error");
    Mockito.when(mockHttpMsgException.getMostSpecificCause()).thenReturn(ex);
    ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(mockHttpMsgException,
                                                                           headers,
                                                                           status.BAD_REQUEST,
                                                                           mockRequest);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

}