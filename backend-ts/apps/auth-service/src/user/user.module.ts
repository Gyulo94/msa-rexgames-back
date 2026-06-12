import { Module } from '@nestjs/common';
import { UserService } from './user.service';
import { UserController } from './user.controller';
import { EmailService } from '../email/email.service';
import { UserRepository } from './user.repository';

@Module({
  controllers: [UserController],
  providers: [UserService, EmailService, UserRepository],
  exports: [UserService, UserRepository],
})
export class UserModule {}
