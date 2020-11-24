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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opengroup.osdu.core.common.exception.BadRequestException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.file.errors.ErrorResponse;
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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  final JaxRsDpsLog log;
  private static final String CORRELATION_ID = "correlation-id";
  @ExceptionHandler({ HttpMessageConversionException.class})
  protected ResponseEntity<Object> handleEnumValidationException(HttpMessageConversionException ex, WebRequest request) {
    String errorMessage = ex.getMostSpecificCause().getLocalizedMessage();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST);
    errorResponse.setCode(400);
    errorResponse.setMessage("Validation Error");
    errorResponse.addErrors(new BadRequestError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }
  @ExceptionHandler(StorageException.class)
  protected ResponseEntity<Object> handleStorageException(StorageException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.resolve(ex.getHttpResponse().getResponseCode()));
    errorResponse.setMessage("Storage record error");
    StorageError storageError  = new StorageError();
    storageError.setErrorProperties(ex.getHttpResponse().getBody());
    errorResponse.addErrors(storageError);
    errorResponse.setCode(ex.getHttpResponse().getResponseCode());
    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }


  @ExceptionHandler(ApplicationException.class)
  protected ResponseEntity<Object> handleApplicationException(ApplicationException ex, WebRequest request) {
    String errorMessage = ex.getErrorMsg();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    errorResponse.setCode(500);
    errorResponse.setMessage(ex.getErrorMsg());
    errorResponse.addErrors(new InternalServerError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }
  /*
   * Triggered when a runtime exception is thrown
   */
  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
    String errorMessage = "Internal server error";
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    errorResponse.setCode(500);
    errorResponse.setMessage(errorMessage);
    errorResponse.addErrors(new InternalServerError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }

  @ExceptionHandler({OsduBadRequestException.class, FileLocationNotFoundException.class,
      LocationAlreadyExistsException.class, BadRequestException.class})
  protected ResponseEntity<Object> handleBadRequest(OsduBadRequestException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST);
    errorResponse.setCode(400);
    errorResponse.setMessage(ex.getMessage());
    errorResponse.addErrors(new BadRequestError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
    String errorMessage = ex.getErrorMsg();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND);
    errorResponse.setCode(404);
    errorResponse.setMessage(errorMessage);
    errorResponse.addErrors(new NotFoundError(errorMessage));

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }

  @ExceptionHandler(OsduUnauthorizedException.class)
  protected ResponseEntity<Object> handleAccessDeniedException(OsduUnauthorizedException ex, WebRequest request) {
    String errorMessage = ex.getMessage();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED);
    errorResponse.setCode(401);
    errorResponse.setMessage(errorMessage);
    errorResponse.addErrors(new AuthorizationError(errorMessage));

    errorMessage = getCorrelationId(request) + errorMessage;
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    String errorMessage = "Parameter validation error :" + ex.getBindingResult().getFieldErrors().toString();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST);
    errorResponse.setCode(400);
    errorResponse.setMessage("Validation Error");
    errorResponse.addValidationErrors(ex.getBindingResult().getFieldErrors());

    errorMessage = errorMessage + getCorrelationId(request);
    log.error(errorMessage);
    log.warning(errorMessage, ex);
    return buildResponseEntity(errorResponse);
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

  protected ResponseEntity<Object> buildResponseEntity(ErrorResponse errorResponse) {
    return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
  }

  protected String getCorrelationId(WebRequest request) {
    return Optional
        .ofNullable(request.getHeader(CORRELATION_ID)).map(value -> value + " : ").orElse("");
  }

}
