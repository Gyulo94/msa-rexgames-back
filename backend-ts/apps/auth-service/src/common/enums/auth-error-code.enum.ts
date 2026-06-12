import { HttpStatus } from '@nestjs/common';

export enum AuthErrorCode {
  USER_ALREADY_EXISTS = 'AUTH_001',
  VERIFICATION_TOKEN_INVALID = 'AUTH_002',
  VERIFICATION_FAILED = 'AUTH_003',
  USER_NOT_FOUND = 'AUTH_004',
  INCORRECT_EMAIL_OR_PASSWORD = 'AUTH_005',
  ALREADY_EXIST_SOCIAL_USER = 'AUTH_006',
  REFRESH_TOKEN_INVALID = 'AUTH_007',
  VERIFICATION_TOKEN_EXPIRED = 'AUTH_008',
  ALREADY_EXIST_LOCAL_USER = 'AUTH_009',
  ONLY_ADMIN_CAN_LOGIN = 'AUTH_010',
}

export const AuthErrorCodeMap: Record<
  AuthErrorCode,
  { status: HttpStatus; message: string }
> = {
  [AuthErrorCode.USER_ALREADY_EXISTS]: {
    status: HttpStatus.CONFLICT,
    message: '이미 존재하는 사용자입니다.',
  },
  [AuthErrorCode.VERIFICATION_TOKEN_INVALID]: {
    status: HttpStatus.BAD_REQUEST,
    message: '유효하지 않은 검증 토큰입니다.',
  },
  [AuthErrorCode.VERIFICATION_FAILED]: {
    status: HttpStatus.BAD_REQUEST,
    message: '검증에 실패했습니다.',
  },
  [AuthErrorCode.USER_NOT_FOUND]: {
    status: HttpStatus.NOT_FOUND,
    message: '사용자를 찾을 수 없습니다.',
  },
  [AuthErrorCode.INCORRECT_EMAIL_OR_PASSWORD]: {
    status: HttpStatus.UNAUTHORIZED,
    message: '이메일 또는 비밀번호가 올바르지 않습니다.',
  },
  [AuthErrorCode.ALREADY_EXIST_SOCIAL_USER]: {
    status: HttpStatus.CONFLICT,
    message: '이미 존재하는 소셜 로그인 사용자입니다.',
  },
  [AuthErrorCode.REFRESH_TOKEN_INVALID]: {
    status: HttpStatus.UNAUTHORIZED,
    message: '유효하지 않은 리프레시 토큰입니다.',
  },
  [AuthErrorCode.VERIFICATION_TOKEN_EXPIRED]: {
    status: HttpStatus.BAD_REQUEST,
    message: '검증 토큰이 만료되었습니다.',
  },
  [AuthErrorCode.ALREADY_EXIST_LOCAL_USER]: {
    status: HttpStatus.CONFLICT,
    message: '이미 존재하는 로컬 로그인 사용자입니다.',
  },
  [AuthErrorCode.ONLY_ADMIN_CAN_LOGIN]: {
    status: HttpStatus.FORBIDDEN,
    message: '관리자만 로그인할 수 있습니다.',
  },
};
