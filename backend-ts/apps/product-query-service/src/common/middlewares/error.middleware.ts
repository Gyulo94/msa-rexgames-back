import { Request, Response, NextFunction } from "express";
import { format } from "date-fns";
import { ApiException } from "../exceptions/api.exception";
import { LOGGER } from "../config/logger";
import { ko } from "date-fns/locale/ko";

export class ErrorMiddleware {
  use(err: any, req: Request, res: Response, next: NextFunction): void {
    let statusCode = 500;
    let message = "서버 내부 오류가 발생했습니다.";
    let errorCode: string | undefined;

    if (err instanceof ApiException) {
      statusCode = err.getStatus?.() || err.statusCode || 500;
      message = err.message;
      errorCode = err.getErrorCode?.();
    } else if (err.status || err.statusCode) {
      statusCode = err.status || err.statusCode;
      message = err.message || message;
    } else {
      message = err.message || message;
      LOGGER.error(err.stack || err, "ErrorMiddleware");
    }

    const logMessage = `[CODE : ${statusCode}] [${req.method} ${req.originalUrl}] / ${message}`;

    if ([400, 401, 403, 404, 409].includes(statusCode)) {
      LOGGER.warn(logMessage, "ErrorMiddleware");
    } else {
      LOGGER.error(logMessage, "ErrorMiddleware");
      if (err.stack) LOGGER.error(err.stack, "ErrorMiddleware");
    }

    const responseBody = {
      statusCode,
      message,
      code: errorCode,
      path: req.originalUrl,
      timestamp: format(new Date(), "yyyy-MM-dd HH:mm:ss", { locale: ko }),
    };

    res.status(statusCode).json(responseBody);
  }
}
