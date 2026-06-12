package com.shop.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.shop.common.api.Api;
import com.shop.common.error.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Api<Object>> handleApiException(ApiException e) {
    log.error("ApiException 발생: [code: {}] {}",
        e.getErrorCodeInterface().getHttpStatusCode(), e.getMessage(), e);

    return ResponseEntity
        .status(e.getErrorCodeInterface().getHttpStatusCode())
        .body(Api.ERROR(e.getErrorCodeInterface(), e.getErrorMessage()));
  }

  @ExceptionHandler({
      NullPointerException.class,
      IllegalArgumentException.class,
      IllegalStateException.class,
      DataIntegrityViolationException.class
  })
  public ResponseEntity<Api<Object>> handleSpecificRuntimeException(RuntimeException e) {
    ErrorCode errorCode = mapToErrorCode(e);

    log.error("{} 발생: {}", errorCode.name(), e.getMessage(), e);

    return ResponseEntity
        .status(errorCode.getHttpStatusCode())
        .body(Api.ERROR(errorCode));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Api<Object>> handleException(Exception e) {
    log.error("알 수 없는 예외 발생", e);
    return ResponseEntity
        .status(500)
        .body(Api.ERROR(ErrorCode.SERVER_ERROR));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Api<Object>> handleNoResourceFoundException(NoResourceFoundException e) {
    log.warn("404 리소스 없음 - Path: {}", e.getResourcePath());
    return ResponseEntity
        .status(404)
        .body(Api.ERROR(ErrorCode.NOT_FOUND));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Api<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.warn("요청 바디 파싱 실패 - Message: {}", e.getMessage());
    return ResponseEntity
        .badRequest()
        .body(Api.ERROR(ErrorCode.BAD_REQUEST, "요청 바디의 형식이 올바르지 않습니다."));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Api<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

    FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);

    String errorMessage = fieldError.getDefaultMessage();

    log.warn("유효성 검사 실패 - 필드: {}, 메시지: {}",
        fieldError.getField(), errorMessage, e);

    return ResponseEntity
        .badRequest()
        .body(Api.ERROR(ErrorCode.INVALID_INPUT, errorMessage));
  }

  private ErrorCode mapToErrorCode(RuntimeException e) {
    if (e instanceof NullPointerException)
      return ErrorCode.NULL_POINT;
    if (e instanceof IllegalArgumentException)
      return ErrorCode.ILLEGAL_ARGUMENT;
    if (e instanceof IllegalStateException)
      return ErrorCode.ILLEGAL_STATE;
    if (e instanceof DataIntegrityViolationException)
      return ErrorCode.DATA_INTEGRITY_VIOLATION;
    return ErrorCode.SERVER_ERROR;
  }
}
