export enum ErrorCode {
  // 일반적인 에러
  INTERNAL_SERVER_ERROR = "SERVER_001",
  BAD_REQUEST = "COMMON_001",
  FORBIDDEN = "COMMON_002",
  UNAUTHORIZED = "COMMON_003",

  // 제품 관련 에러
  PRODUCT_NOT_FOUND = "PRODUCT_001",
}

export const ErrorCodeMap: Record<
  ErrorCode,
  { status: number; message: string }
> = {
  // 일반적인 에러
  [ErrorCode.INTERNAL_SERVER_ERROR]: {
    status: 500,
    message: "서버 내부 오류가 발생했습니다.",
  },
  [ErrorCode.BAD_REQUEST]: {
    status: 400,
    message: "잘못된 요청입니다.",
  },
  [ErrorCode.FORBIDDEN]: {
    status: 403,
    message: "접근이 거부되었습니다.",
  },
  [ErrorCode.UNAUTHORIZED]: {
    status: 401,
    message: "인증이 필요합니다.",
  },

  // 제품 관련 에러
  [ErrorCode.PRODUCT_NOT_FOUND]: {
    status: 404,
    message: "요청하신 제품을 찾을 수 없습니다.",
  },
};
