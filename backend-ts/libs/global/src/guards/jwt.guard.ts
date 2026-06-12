import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { Request } from 'express';
import { ApiException } from '../exceptions/api.exception';
import { ErrorCode } from '../enums/error-code.enum';
import { ACCESS_TOKEN_SECRET_KEY, IS_PUBLIC_KEY } from '..';

@Injectable()
export class JwtGuard implements CanActivate {
  constructor(
    private jwtService: JwtService,
    private readonly reflector: Reflector,
  ) {}
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) {
      return true;
    }
    const request = context.switchToHttp().getRequest();
    const token = this.extractTokenFromHeader(request);

    if (!token) throw new ApiException(ErrorCode.UNAUTHORIZED);
    try {
      const payload = await this.jwtService.verifyAsync(token, {
        secret: ACCESS_TOKEN_SECRET_KEY,
      });
      request['user'] = payload;
    } catch (error) {
      throw new ApiException(ErrorCode.UNAUTHORIZED);
    }
    return true;
  }

  private extractTokenFromHeader(request: Request) {
    const authorization = request.headers.authorization;
    if (!authorization) throw new ApiException(ErrorCode.UNAUTHORIZED);
    const [type, token] = authorization.split(' ') ?? [];
    return type === 'Bearer' ? token : null;
  }
}
