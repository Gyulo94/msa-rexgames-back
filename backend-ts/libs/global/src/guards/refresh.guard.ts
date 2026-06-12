import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { Request } from 'express';
import { ApiException } from '../exceptions/api.exception';
import { ErrorCode } from '../enums/error-code.enum';

@Injectable()
export class RefreshJwtGuard implements CanActivate {
  constructor(private jwtService: JwtService) {}
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const token = this.extractTokenFromHeader(request);
    if (!token) throw new ApiException(ErrorCode.UNAUTHORIZED);
    try {
      const payload = await this.jwtService.verifyAsync(token, {
        secret: process.env.REFRESH_TOKEN_SECRET_KEY,
      });
      const { exp, iat, nbf, ...rest } = payload;
      request['user'] = rest;
    } catch (error) {
      throw new ApiException(ErrorCode.UNAUTHORIZED);
    }
    return true;
  }

  private extractTokenFromHeader(request: Request) {
    const authorization = request.headers.authorization;
    if (!authorization) throw new ApiException(ErrorCode.UNAUTHORIZED);
    const [type, token] = authorization.split(' ') ?? [];
    return type === 'Refresh' ? token : undefined;
  }
}
