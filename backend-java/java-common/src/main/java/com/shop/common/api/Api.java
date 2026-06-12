package com.shop.common.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shop.common.error.ErrorCode;
import com.shop.common.error.ErrorCodeInterface;
import com.shop.common.message.ResponseMessageInterface;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
public class Api<T> {

  private int statusCode;
  private String message;
  private String timestamp;
  private String method;
  private String path;
  private T body;

  private Api(int statusCode, String message, String timestamp, String path, String method, T body) {
    this.statusCode = statusCode;
    this.message = message;
    this.timestamp = Objects.requireNonNullElse(timestamp, getCurrentTimestamp());
    this.path = Objects.requireNonNullElse(path, getCurrentRequestUri());
    this.method = Objects.requireNonNullElse(method, getCurrentMethod());
    this.body = body;
  }

  private static <T> Api<T> build(int statusCode, String message, T body) {
    return Api.<T>builder()
        .statusCode(statusCode)
        .message(message)
        .body(body)
        .build();
  }

  public static Api<Void> OK() {
    return build(200, ErrorCode.OK.getMessage(), null);
  }

  public static <T> Api<T> OK(T data) {
    return build(200, ErrorCode.OK.getMessage(), data);
  }

  public static <T> Api<T> OK(T data, ResponseMessageInterface responseMessage) {
    String message = (responseMessage != null) ? responseMessage.getMessage() : ErrorCode.OK.getMessage();
    return build(ErrorCode.OK.getHttpStatusCode(), message, data);
  }

  public static Api<Void> OK(ResponseMessageInterface responseMessage) {
    String message = (responseMessage != null) ? responseMessage.getMessage() : ErrorCode.OK.getMessage();
    return build(ErrorCode.OK.getHttpStatusCode(), message, null);
  }

  public static Api<Object> ERROR(ErrorCodeInterface errorCodeInterface) {
    return build(errorCodeInterface.getHttpStatusCode(), errorCodeInterface.getMessage(), null);
  }

  public static Api<Object> ERROR(ErrorCodeInterface errorCodeInterface, String message) {
    return build(errorCodeInterface.getHttpStatusCode(), message, null);
  }

  public static Api<Object> ERROR(int statusCode, ResponseMessageInterface responseMessage) {
    return build(statusCode, responseMessage.getMessage(), null);
  }

  private static String getCurrentRequestUri() {
    try {
      ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
      return sra.getRequest().getRequestURI();
    } catch (IllegalStateException e) {
      return "N/A";
    }
  }

  private static String getCurrentMethod() {
    try {
      ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
      return sra.getRequest().getMethod();
    } catch (IllegalStateException e) {
      return "N/A";
    }
  }

  private static String getCurrentTimestamp() {
    return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
