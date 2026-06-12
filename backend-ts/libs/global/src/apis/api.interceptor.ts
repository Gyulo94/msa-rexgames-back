import {
  CallHandler,
  ExecutionContext,
  HttpStatus,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Api } from './api.interface';
import { format } from 'date-fns/format';
import { SUCCESS_MESSAGE_METADATA } from '../decorators/message.decorator';

@Injectable()
export class ApiInterceptor<T> implements NestInterceptor<T, Api<T>> {
  constructor(private readonly reflector: Reflector) {}

  intercept(
    context: ExecutionContext,
    next: CallHandler<T>,
  ): Observable<Api<T>> {
    if (context.getType() !== 'http') {
      return next.handle() as Observable<Api<T>>;
    }

    const ctx = context.switchToHttp();
    const request = ctx.getRequest();
    const response = ctx.getResponse();

    return next.handle().pipe(
      map((payload: T) => {
        const staticMessage = this.reflector.get<string>(
          SUCCESS_MESSAGE_METADATA,
          context.getHandler(),
        );

        let message = staticMessage || '성공';
        let body: T | null = payload;

        if (typeof payload === 'string') {
          message = payload;
          body = null;
        } else if (
          payload &&
          typeof payload === 'object' &&
          payload !== null &&
          'message' in payload
        ) {
          const payloadObj = payload as {
            message: string;
            body?: T | null;
          };

          if (typeof payloadObj.message === 'string') {
            message = payloadObj.message;
            body = 'body' in payloadObj ? (payloadObj.body ?? null) : payload;
          }
        }

        return {
          statusCode: response.statusCode || HttpStatus.OK,
          method: request.method,
          path: request.url,
          message,
          body,
          timestamp: format(new Date(), 'yyyy-MM-dd HH:mm:ss'),
        } as Api<T>;
      }),
    );
  }
}
