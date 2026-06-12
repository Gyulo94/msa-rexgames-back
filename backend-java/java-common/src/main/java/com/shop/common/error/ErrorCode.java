package com.shop.common.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeInterface {
  OK(HttpStatus.OK.value(), "성공"),
  BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."),
  SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 에러가 발생했습니다."),
  NULL_POINT(HttpStatus.INTERNAL_SERVER_ERROR.value(), "널 포인트 에러가 발생했습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN.value(), "접근이 거부되었습니다."),
  ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST.value(), "잘못된 인자가 전달되었습니다."),
  ILLEGAL_STATE(HttpStatus.BAD_REQUEST.value(), "잘못된 상태에서 메서드가 호출되었습니다."),
  DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT.value(), "데이터 무결성 위반 오류입니다."),
  INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "입력값이 유효하지 않습니다."),
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청하신 리소스를 찾을 수 없습니다."),

  // 이미지 관련 에러
  SAVE_IMAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "이미지 저장에 실패했습니다."),

  // 장르 및 플랫폼 관련 에러
  GENRE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 장르입니다."),
  PLATFORM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 플랫폼입니다."),

  // 상품 관련 에러
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 상품입니다."),

  // 주문 관련 에러
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 주문입니다."),

  // 결제 관련 에러
  PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 처리에 실패했습니다.");

  private final Integer httpStatusCode;
  private final String message;
}
