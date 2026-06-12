import { Injectable, Logger, NestMiddleware } from '@nestjs/common';
import { NextFunction, Request, Response } from 'express';
@Injectable()
export class RequestMiddleware implements NestMiddleware {
  constructor(private readonly logger: Logger) {}
  use(req: Request, res: Response, next: NextFunction) {
    if (process.env.NODE_ENV === 'development') {
      this.logger.log(
        JSON.stringify({
          url: req.url,
          method: req.method,
          body: req.body,
        }),
      );
    } else {
      this.logger.log(
        JSON.stringify({
          url: req.url,
          method: req.method,
          ip: req.ip,
        }),
      );
    }
    next();
  }
}
