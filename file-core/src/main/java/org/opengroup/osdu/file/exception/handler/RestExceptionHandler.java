/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.file.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.errors.Error;
import org.opengroup.osdu.file.errors.model.*;
import org.opengroup.osdu.file.exception.*;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  final JaxRsDpsLog log;
  private static final String CORRELATION_ID = "correlation-id";
  @ExceptionHandler({ HttpMessageConversionException.class})
  protected ResponseEntity<Object> handleEnumValidationException(HttpMessageConversionException ex, WebRequest request) {
    String errorMessage = ex.getMostSpecificCause().getLocalizedMessage();
    Error error = new Error(HttpStatus.BAD_REQUEST);
    error.setCode(400);
    error.setMessage("Validation Error");
    error.addErrors(new BadRequestError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }
  @ExceptionHandler(StorageException.class)
  protected ResponseEntity<Object> handleStorageException(StorageException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    Error error = new Error(HttpStatus.resolve(ex.getHttpResponse().getResponseCode()));
    error.setMessage("Storage record error");
    StorageError storageError  = new StorageError();
    storageError.setErrorProperties(ex.getHttpResponse().getBody());
    error.addErrors(storageError);
    error.setCode(ex.getHttpResponse().getResponseCode());
    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }


  @ExceptionHandler(ApplicationException.class)
  protected ResponseEntity<Object> handleApplicationException(ApplicationException ex, WebRequest request) {
    String errorMessage = ex.getErrorMsg();
    Error error = new Error(HttpStatus.INTERNAL_SERVER_ERROR);
    error.setCode(500);
    error.setMessage(ex.getErrorMsg());
    error.addErrors(new InternalServerError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }
  /*
   * Triggered when a runtime exception is thrown
   */
  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
    String errorMessage = "Internal server error";
    Error error = new Error(HttpStatus.INTERNAL_SERVER_ERROR);
    error.setCode(500);
    error.setMessage(errorMessage);
    error.addErrors(new InternalServerError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }

  @ExceptionHandler(OsduBadRequestException.class)
  protected ResponseEntity<Object> handleBadRequest(OsduBadRequestException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    Error error = new Error(HttpStatus.BAD_REQUEST);
    error.setCode(400);
    error.setMessage(ex.getMessage());
    error.addErrors(new BadRequestError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
    String errorMessage = ex.getErrorMsg();
    Error error = new Error(HttpStatus.NOT_FOUND);
    error.setCode(404);
    error.setMessage(errorMessage);
    error.addErrors(new NotFoundError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }

  @ExceptionHandler(OsduUnauthorizedException.class)
  protected ResponseEntity<Object> handleAccessDeniedException(OsduUnauthorizedException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    Error error = new Error(HttpStatus.UNAUTHORIZED);
    error.setCode(401);
    error.setMessage(errorMessage);
    error.addErrors(new AuthorizationError(errorMessage));

    errorMessage = getCorrelationId(request) + errorMessage;
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    String errorMessage = "Parameter validation error :" + ex.getBindingResult().getFieldErrors().toString();
    Error error = new Error(HttpStatus.BAD_REQUEST);
    error.setCode(400);
    error.setMessage("Validation Error");
    error.addValidationErrors(ex.getBindingResult().getFieldErrors());

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(error);
  }

  @ExceptionHandler({ JsonParseException.class, IllegalStateException.class,
      MismatchedInputException.class, IllegalArgumentException.class })
  protected ResponseEntity<Object> handleInvalidBody(RuntimeException ex,
      WebRequest request) {
    log.error("Exception during REST request: " + request.getDescription(false), ex);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .build();
    return handleExceptionInternal(ex, apiError, headers,
        HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({ ConstraintViolationException.class })
  protected ResponseEntity<Object> handle(ConstraintViolationException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String propertyPath = Objects.toString(violation.getPropertyPath(), "");
      String propertyPart = StringUtils.isEmpty(propertyPath) ? "" : propertyPath + ": ";
      errors.add(propertyPart + violation.getMessage());
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .errors(errors)
        .build();
    return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(AppException.class)
  protected ResponseEntity<Object> handleAppException(AppException e) {
    return this.getErrorResponse(e);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request) {
    ApiError apiError = ApiError.builder()
                                .status(status)
                                .message("Invalid Json Input")
                                .build();
    return handleExceptionInternal(ex, apiError, headers, status, request);
  }

  private ResponseEntity<Object> getErrorResponse(AppException e) {

    String exceptionMsg = e.getOriginalException() != null
        ? e.getOriginalException().getMessage()
        : e.getError().getMessage();

    if (e.getError().getCode() > 499) {
      this.log.error(exceptionMsg, e);
    } else {
      this.log.warning(exceptionMsg, e);
    }

    // Support for non standard HttpStatus Codes
    HttpStatus httpStatus = HttpStatus.resolve(e.getError().getCode());
    if (httpStatus == null) {
      return ResponseEntity.status(e.getError().getCode()).body(e);
    } else {
      return new ResponseEntity<>(e.getError(), httpStatus);
    }
  }

  protected ResponseEntity<Object> buildResponseEntity(Error error) {
    return new ResponseEntity<>(error, error.getStatus());
  }

  protected String getCorrelationId(WebRequest request) {
    return Optional
        .ofNullable(request.getHeader(CORRELATION_ID)).map(value -> value + " : ").orElse("");
  }

}
