package com.shop.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage implements ResponseMessageInterface {
  CREATE_PRODUCT_SUCCESS("상품이 성공적으로 생성되었습니다."),
  UPDATE_PRODUCT_SUCCESS("상품이 성공적으로 수정되었습니다."),
  DELETE_PRODUCT_SUCCESS("상품이 성공적으로 삭제되었습니다."),
  CREATE_ORDER_SUCCESS("주문이 성공적으로 완료되었습니다."),
  PAYMENT_SUCCESS("결제가 성공적으로 처리되었습니다.");

  private final String message;

}
