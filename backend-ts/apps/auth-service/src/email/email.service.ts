import { v4 as uuid } from 'uuid';
import axios from 'axios';
import { Injectable } from '@nestjs/common';
import {
  CLIENT_URL,
  EMAIL_SERVICE_URL,
  PROJECT_LOGO,
  PROJECT_NAME,
  RedisKey,
  SMTP_PASS,
  SMTP_USER,
  ApiException,
  ErrorCode,
  RedisService,
} from '@app/global';

export interface SendVerificationParams {
  email: string;
  type: string;
  payload: any;
}

@Injectable()
export class EmailService {
  constructor(private readonly redis: RedisService) {}

  async sendVerificationEmail({
    email,
    type,
    payload,
  }: SendVerificationParams) {
    const token = uuid();

    let redisKey: string | undefined;
    if (type === 'register') {
      redisKey = RedisKey.verificationRegister(token);
    } else if (type === 'reset') {
      redisKey = RedisKey.verificationReset(token);
    }

    if (!redisKey) {
      throw new ApiException(ErrorCode.BAD_REQUEST);
    }

    await this.redis.set(redisKey, JSON.stringify(payload), 900);

    const path =
      type === 'register' ? 'register/verify' : 'reset-password/verify';
    const url = `${CLIENT_URL}/${path}?token=${token}`;

    try {
      await axios.post(EMAIL_SERVICE_URL || '', {
        serviceName: PROJECT_NAME,
        logo: PROJECT_LOGO,
        senderEmail: SMTP_USER,
        senderPwd: SMTP_PASS,
        email,
        type,
        url,
      });
    } catch (error) {
      await this.redis.del(redisKey);
      console.error('이메일 발송 실패: ', error);
    }
  }
}
