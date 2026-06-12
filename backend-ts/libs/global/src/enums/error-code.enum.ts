import { HttpStatus } from '@nestjs/common';

export enum ErrorCode {
  // 일반적인 에러
  INTERNAL_SERVER_ERROR = 'SERVER_001',
  BAD_REQUEST = 'COMMON_001',
  FORBIDDEN = 'COMMON_002',
  UNAUTHORIZED = 'COMMON_003',
}

export const ErrorCodeMap: Record<
  ErrorCode,
  { status: HttpStatus; message: string }
> = {
  // 일반적인 에러
  [ErrorCode.INTERNAL_SERVER_ERROR]: {
    status: HttpStatus.INTERNAL_SERVER_ERROR,
    message: '서버 내부 오류가 발생했습니다.',
  },
  [ErrorCode.BAD_REQUEST]: {
    status: HttpStatus.BAD_REQUEST,
    message: '잘못된 요청입니다.',
  },
  [ErrorCode.FORBIDDEN]: {
    status: HttpStatus.FORBIDDEN,
    message: '접근이 거부되었습니다.',
  },
  [ErrorCode.UNAUTHORIZED]: {
    status: HttpStatus.UNAUTHORIZED,
    message: '인증이 필요합니다.',
  },
};
