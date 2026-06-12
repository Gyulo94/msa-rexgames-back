import { Request, Response, NextFunction } from "express";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

export interface Api<T = any> {
  statusCode: number;
  message: string;
  timestamp: string;
  method: string;
  path: string;
  body: T | null;
}

export class ApiMiddleware {
  public readonly use = (
    req: Request,
    res: Response,
    next: NextFunction,
  ): void => {
    const originalJson = res.json.bind(res);

    res.json = function (data: any): Response {
      let message = "성공";
      let body: any = data;

      if (typeof data === "string") {
        message = data;
        body = null;
      } else if (data && typeof data === "object" && "message" in data) {
        message = data.message;
        body = "body" in data ? data.body : null;
      } else {
        body = data;
      }

      const response: Api = {
        statusCode: res.statusCode || 200,
        message,
        timestamp: format(new Date(), "yyyy-MM-dd HH:mm:ss", { locale: ko }),
        method: req.method,
        path: req.originalUrl,
        body,
      };

      return originalJson(response);
    };

    next();
  };
}
