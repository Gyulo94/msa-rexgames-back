import { HttpException, HttpStatus } from '@nestjs/common';
import { AuthErrorCode, AuthErrorCodeMap } from '..';

export class ApiException extends HttpException {
  constructor(public readonly errorCode: AuthErrorCode) {
    const detail = AuthErrorCodeMap[errorCode];
    super(
      detail ? detail.message : '알 수 없는 에러가 발생했습니다.',
      detail ? detail.status : HttpStatus.BAD_REQUEST,
    );
  }

  getErrorCode(): AuthErrorCode {
    return this.errorCode;
  }
}
