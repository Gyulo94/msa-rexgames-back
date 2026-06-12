import { Request, Response, NextFunction } from "express";
import { LOGGER } from "../config/logger";

export class LoggerMiddleware {
  public readonly use = (
    req: Request,
    res: Response,
    next: NextFunction,
  ): void => {
    const { method, originalUrl, body, query, ip } = req;

    const safeBody = this.sanitizeBody(body);

    LOGGER.http(`${method} ${originalUrl}`, {
      context: "LoggerMiddleware",
      ip: ip || req.socket.remoteAddress,
      ...(Object.keys(query).length && { query }),
      ...(Object.keys(safeBody).length && { body: safeBody }),
    });

    next();
  };

  private sanitizeBody(body: any): any {
    if (!body || typeof body !== "object") return {};

    const sanitized = { ...body };
    const sensitive = ["password", "accessToken", "refreshToken"];

    sensitive.forEach((key) => {
      if (key in sanitized) sanitized[key] = "[안알랴줌]";
    });

    return sanitized;
  }
}
