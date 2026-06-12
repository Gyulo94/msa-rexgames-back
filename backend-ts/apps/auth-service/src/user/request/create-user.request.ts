import { Prisma } from '@prisma/client-auth';
import { IsEmail, IsNotEmpty, IsString } from 'class-validator';

export class CreateUserRequest {
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsNotEmpty()
  @IsString()
  password: string;

  @IsNotEmpty()
  @IsString()
  name: string;

  constructor(email: string, password: string, name: string) {
    this.email = email;
    this.password = password;
    this.name = name;
  }

  static toEntity(
    request: CreateUserRequest,
    hashedPassword: string,
  ): Prisma.UserCreateInput {
    const userEntity: Prisma.UserCreateInput = {
      email: request.email,
      password: hashedPassword,
      name: request.name,
    };
    return userEntity;
  }
}
