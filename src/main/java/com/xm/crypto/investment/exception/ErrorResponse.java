package com.xm.crypto.investment.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private final ErrorType errorType;
  private final String message;
  private final List<ValidationError> validationErrors;
  private final Integer httpCode;
  private final Instant timestamp;

  public ErrorResponse(ErrorType errorType, String message, List<ValidationError> validationErrors, Integer httpCode, Instant timestamp) {
    this.errorType = errorType;
    this.message = message;
    this.validationErrors = validationErrors;
    this.httpCode = httpCode;
    this.timestamp = timestamp;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public String getMessage() {
    return message;
  }

  public List<ValidationError> getValidationErrors() {
    return validationErrors;
  }

  public Integer getHttpCode() {
    return httpCode;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public enum ErrorType {
    GENERAL,
    NOT_FOUND,
    VALIDATION_ERROR
  }

  public static class ValidationError {
    private final String field;
    private final String message;

    public ValidationError(String field, String message) {
      this.field = field;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public String getMessage() {
      return message;
    }
  }
}
