import { Prisma } from '@prisma/client-auth';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export class ResetPasswordRequest {
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsString()
  @IsNotEmpty()
  token: string;

  @IsString()
  @IsNotEmpty()
  @MinLength(8)
  newPassword: string;

  constructor(email: string, token: string, newPassword: string) {
    this.email = email;
    this.token = token;
    this.newPassword = newPassword;
  }

  static toEntity(userId: number, newPassword: string): Prisma.UserUpdateArgs {
    return {
      where: {
        id: userId,
      },
      data: {
        password: newPassword,
      },
    };
  }
}
