import { HttpStatus } from '@nestjs/common';

export enum CartErrorCode {
  PRODUCT_NOT_FOUND = 'CART_001',
}

export const CartErrorCodeMap: Record<
  CartErrorCode,
  { status: HttpStatus; message: string }
> = {
  [CartErrorCode.PRODUCT_NOT_FOUND]: {
    status: HttpStatus.NOT_FOUND,
    message: '상품을 찾을 수 없습니다.',
  },
};
