import { Injectable } from '@nestjs/common';
import { CreateUserRequest } from './request/create-user.request';
import { ApiException, AuthErrorCode } from '../common';
import { EmailService } from '../email/email.service';
import { UserRepository } from './user.repository';
import { User } from '@prisma/client-auth/client';
import * as bcrypt from 'bcryptjs';
import { ResetPasswordRequest } from '../auth/request/reset-password.request';
import { RedisKey, RedisService } from '@app/global';
import { SocialLoginRequest } from '../auth/request/social-login.request';

@Injectable()
export class UserService {
  constructor(
    private readonly userRepository: UserRepository,
    private readonly emailService: EmailService,
    private readonly redis: RedisService,
  ) {}

  async findByEmail(email: string) {
    return this.userRepository.findByEmail(email);
  }

  async register(request: CreateUserRequest) {
    const existingUser = await this.findByEmail(request.email);
    if (existingUser) {
      throw new ApiException(AuthErrorCode.USER_ALREADY_EXISTS);
    }
    this.emailService.sendVerificationEmail({
      email: request.email,
      type: 'register',
      payload: request,
    });
  }

  async create(request: CreateUserRequest): Promise<User> {
    const hashedPassword = await bcrypt.hash(request.password, 10);
    return this.userRepository.create(
      CreateUserRequest.toEntity(request, hashedPassword),
    );
  }

  async resetPassword(request: ResetPasswordRequest): Promise<void> {
    const { email, token, newPassword } = request;
    const redisKey = RedisKey.verificationReset(token);
    const user = await this.findByEmail(email);
    if (!user) {
      throw new ApiException(AuthErrorCode.USER_NOT_FOUND);
    }

    const hashedPassword = await bcrypt.hash(newPassword, 10);
    await this.userRepository.resetPassword(
      ResetPasswordRequest.toEntity(user.id, hashedPassword),
    );
    await this.redis.del(redisKey);
  }

  async findOrCreateSocialUser(request: SocialLoginRequest): Promise<User> {
    const email = request.email!;
    const existingUser = await this.findByEmail(email);

    if (existingUser) {
      if (existingUser.provider !== 'LOCAL') {
        return existingUser;
      } else if (existingUser.provider === 'LOCAL') {
        throw new ApiException(AuthErrorCode.ALREADY_EXIST_LOCAL_USER);
      }

      throw new ApiException(AuthErrorCode.ALREADY_EXIST_SOCIAL_USER);
    }

    return this.userRepository.createSocialUser(
      SocialLoginRequest.toEntity(request),
    );
  }
}
