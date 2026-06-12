import { Injectable, Logger } from '@nestjs/common';
import { CreateUserRequest } from '../user/request/create-user.request';
import { UserService } from '../user/user.service';
import { ApiException, AuthErrorCode } from '../common';
import { JwtPayload, RedisService } from '@app/global';
import { AuthRequest } from './request/auth.request';
import { User } from '@prisma/client-auth';
import { AuthResponse } from './response/auth.response';
import { JwtService } from '@nestjs/jwt';
import { TokenResponse } from './response/token.response';
import * as bcrypt from 'bcryptjs';
import { EmailService } from '../email/email.service';
import { ResetPasswordRequest } from './request/reset-password.request';
import { SocialLoginRequest } from './request/social-login.request';
import {
  ACCESS_TOKEN_SECRET_KEY,
  REFRESH_TOKEN_SECRET_KEY,
  RedisKey,
} from '@app/global';

@Injectable()
export class AuthService {
  constructor(
    private readonly userService: UserService,
    private readonly redis: RedisService,
    private readonly jwtService: JwtService,
    private readonly emailService: EmailService,
  ) {}

  private readonly LOGGER = new Logger(AuthService.name);

  register(request: CreateUserRequest) {
    return this.userService.register(request);
  }

  verify(token: string, type: string) {
    if (!token) {
      throw new ApiException(AuthErrorCode.VERIFICATION_TOKEN_INVALID);
    }

    if (type === 'register') {
      return this.verifyRegister(token);
    } else if (type === 'reset') {
      return this.verifyResetPassword(token);
    }

    throw new ApiException(AuthErrorCode.VERIFICATION_FAILED);
  }

  private async verifyRegister(token: string) {
    const redisKey = RedisKey.verificationRegister(token);
    const cachedUserData = await this.redis.get(redisKey);
    if (!cachedUserData) {
      throw new ApiException(AuthErrorCode.VERIFICATION_TOKEN_INVALID);
    }

    const userData = JSON.parse(cachedUserData) as CreateUserRequest;

    const existingUser = await this.userService.findByEmail(userData.email);
    if (existingUser) {
      await this.redis.del(redisKey);
      throw new ApiException(AuthErrorCode.USER_ALREADY_EXISTS);
    }

    await this.userService.create(userData);

    await this.redis.del(redisKey);
  }

  async login(request: AuthRequest) {
    const user: Omit<User, 'password'> = await this.validateUser(request);
    const redisKey = RedisKey.userRefreshToken(user.id);

    const payload = {
      id: user.id,
      role: user.role,
    };

    const tokenResponse = await this.generateTokens(payload);
    const response = AuthResponse.fromEntity(user, tokenResponse);

    const existingToken = await this.redis.get(redisKey);
    if (existingToken) {
      await this.redis.del(redisKey);
    }
    await this.redis.set(
      redisKey,
      tokenResponse.refreshToken,
      7 * 24 * 60 * 60,
    );

    return response;
  }

  async adminLogin(request: AuthRequest) {
    const user: Omit<User, 'password'> = await this.validateUser(request);
    if (user.role !== 'ADMIN') {
      throw new ApiException(AuthErrorCode.ONLY_ADMIN_CAN_LOGIN);
    }
    const redisKey = RedisKey.userRefreshToken(user.id);

    const payload = {
      id: user.id,
      role: user.role,
    };

    const tokenResponse = await this.generateTokens(payload);
    const response = AuthResponse.fromEntity(user, tokenResponse);

    const existingToken = await this.redis.get(redisKey);
    if (existingToken) {
      await this.redis.del(redisKey);
    }
    await this.redis.set(
      redisKey,
      tokenResponse.refreshToken,
      7 * 24 * 60 * 60,
    );

    return response;
  }

  async refresh(
    user: JwtPayload,
    oldRefreshToken: string,
  ): Promise<TokenResponse> {
    const cachedTokens = await this.redis.get(
      RedisKey.cachedTokens(oldRefreshToken),
    );
    if (cachedTokens) {
      return JSON.parse(cachedTokens) as TokenResponse;
    }

    const redisKey = RedisKey.userRefreshToken(user.id);
    const storedRefreshToken = await this.redis.get(redisKey);

    if (!storedRefreshToken || storedRefreshToken !== oldRefreshToken) {
      throw new ApiException(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }

    const newTokens = await this.generateTokens(user);
    await Promise.all([
      this.redis.set(redisKey, newTokens.refreshToken, 7 * 24 * 60 * 60),
      this.redis.set(
        RedisKey.cachedTokens(oldRefreshToken),
        JSON.stringify(newTokens),
        5,
      ),
    ]);
    return newTokens;
  }

  async logout(userId: number) {
    const redisKey = RedisKey.userRefreshToken(userId);
    await this.redis.del(redisKey);
  }

  async sendResetPasswordMail(email: string): Promise<void> {
    const existingUser = await this.userService.findByEmail(email);
    if (!existingUser) {
      throw new ApiException(AuthErrorCode.USER_NOT_FOUND);
    }

    this.emailService.sendVerificationEmail({
      email,
      type: 'reset',
      payload: email,
    });
  }

  async resetPassword(request: ResetPasswordRequest): Promise<void> {
    return this.userService.resetPassword(request);
  }

  async socialLogin(request: SocialLoginRequest): Promise<AuthResponse> {
    const user = await this.userService.findOrCreateSocialUser(request);
    const payload: JwtPayload = { id: user.id, role: user.role };
    const tokens = await this.generateTokens(payload);
    const response: AuthResponse = AuthResponse.fromEntity(user, tokens);
    return response;
  }

  private async verifyResetPassword(token: string) {
    const redisKey = RedisKey.verificationReset(token);
    const emailStr = await this.redis.get(redisKey);
    if (!emailStr) {
      throw new ApiException(AuthErrorCode.VERIFICATION_TOKEN_EXPIRED);
    }

    const email = JSON.parse(emailStr) as string;

    return { email };
  }

  private async generateTokens(payload: JwtPayload): Promise<TokenResponse> {
    const accessToken = this.jwtService.sign(payload, {
      secret: ACCESS_TOKEN_SECRET_KEY!,
      expiresIn: '15m',
    });
    const refreshToken = this.jwtService.sign(payload, {
      secret: REFRESH_TOKEN_SECRET_KEY!,
      expiresIn: '7d',
    });
    const expiresIn = new Date().getTime() + 15 * 60 * 1000;
    return {
      accessToken,
      refreshToken,
      expiresIn,
    };
  }

  private async validateUser(request: AuthRequest) {
    const { email, password } = request;
    const user = await this.userService.findByEmail(email);
    if (user && user.password && bcrypt.compareSync(password, user.password)) {
      const { password, ...result } = user;
      return result;
    } else if (user && user.provider !== 'LOCAL') {
      throw new ApiException(AuthErrorCode.ALREADY_EXIST_SOCIAL_USER);
    } else {
      throw new ApiException(AuthErrorCode.INCORRECT_EMAIL_OR_PASSWORD);
    }
  }
}
