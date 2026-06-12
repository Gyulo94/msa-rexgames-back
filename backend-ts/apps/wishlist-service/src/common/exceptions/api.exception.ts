import { HttpException, HttpStatus } from '@nestjs/common';
import { WishlistErrorCode, WishlistErrorCodeMap } from '..';

export class ApiException extends HttpException {
  constructor(public readonly errorCode: WishlistErrorCode) {
    const detail = WishlistErrorCodeMap[errorCode];
    super(
      detail ? detail.message : '알 수 없는 에러가 발생했습니다.',
      detail ? detail.status : HttpStatus.BAD_REQUEST,
    );
  }

  getErrorCode(): WishlistErrorCode {
    return this.errorCode;
  }
}
