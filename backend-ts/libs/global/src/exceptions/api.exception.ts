import { HttpException, HttpStatus } from '@nestjs/common';
import { ErrorCode, ErrorCodeMap } from '../enums/error-code.enum';


export class ApiException extends HttpException {
  private readonly errorCode: ErrorCode;

  constructor(errorCode: ErrorCode, description?: string) {
    const errorDetail = ErrorCodeMap[errorCode];
    if (!errorDetail) {
      super(
        '알 수 없는 에러가 발생했습니다.',
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
      this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    } else {
      super(description || errorDetail.message, errorDetail.status);
      this.errorCode = errorCode;
    }
  }

  getErrorCode(): ErrorCode {
    return this.errorCode;
  }
}
