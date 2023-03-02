package com.xm.crypto.investment.controller;

import com.xm.crypto.investment.exception.CryptoNotSupportedException;
import com.xm.crypto.investment.exception.ErrorResponse;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler.
 */
@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex) {

    log.warn("Validation error.", ex);
    return buildErrorResponse("Validation error. Check 'validationErrors'.",
      HttpStatus.BAD_REQUEST,
      ErrorResponse.ErrorType.VALIDATION_ERROR,
      getValidationErrors(ex));
  }

  @ExceptionHandler({CryptoNotSupportedException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  protected ResponseEntity<ErrorResponse> handleCryptoNotSupported(
    Exception ex) {
    log.warn("Entity not found.", ex);
    return buildErrorResponse(ex.getMessage(),
      HttpStatus.NOT_FOUND, ErrorResponse.ErrorType.NOT_FOUND, null);
  }


  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
    Exception ex) {
    log.error("Exception: ", ex);
    return buildErrorResponse(ex.getMessage(),
      HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.ErrorType.GENERAL,
      null);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
    MethodArgumentTypeMismatchException ex) {

    log.warn("Error in parameter validation.", ex);
    return buildErrorResponse(
      String.format("Error in parameter validation. Invalid format for filed: %s", ex.getName()),
      HttpStatus.BAD_REQUEST,
      ErrorResponse.ErrorType.VALIDATION_ERROR,
      null);
  }

  protected List<ErrorResponse.ValidationError> getValidationErrors(
    MethodArgumentNotValidException ex) {

    return ex.getFieldErrors().stream()
      .map(fieldError -> new ErrorResponse.ValidationError(
        fieldError.getField(), fieldError.getDefaultMessage()))
      .collect(Collectors.toList());
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
    String message,
    HttpStatus status,
    ErrorResponse.ErrorType errorType,
    List<ErrorResponse.ValidationError> fieldErrors) {

    return ResponseEntity.status(status)
      .body(new ErrorResponse(errorType, message,
        fieldErrors, status.value(), Instant.now()));
  }

}
