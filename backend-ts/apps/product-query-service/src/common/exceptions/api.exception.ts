import { ErrorCodeMap } from "../enums/error-code.enum";
import { ErrorCode } from "../enums/error-code.enum";

export class ApiException extends Error {
  public readonly statusCode: number;
  public readonly errorCode: ErrorCode;
  public readonly timestamp: string;

  constructor(errorCode: ErrorCode, description?: string) {
    const errorDetail = ErrorCodeMap[errorCode];

    if (!errorDetail) {
      super("알 수 없는 에러가 발생했습니다.");
      this.statusCode = 500;
      this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    } else {
      super(description || errorDetail.message);
      this.statusCode = errorDetail.status;
      this.errorCode = errorCode;
    }

    this.name = "ApiException";
    this.timestamp = new Date().toISOString();

    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, ApiException);
    }
  }

  public getErrorCode(): ErrorCode {
    return this.errorCode;
  }

  public getStatus(): number {
    return this.statusCode;
  }

  public getErrorDetail() {
    return {
      errorCode: this.errorCode,
      statusCode: this.statusCode,
      message: this.message,
      timestamp: this.timestamp,
    };
  }
}
