import {
  IsEmail,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
} from 'class-validator';
import { Prisma, Provider } from '@prisma/client-auth';

export class SocialLoginRequest {
  @IsOptional()
  id?: string | number;

  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsString()
  @IsNotEmpty()
  name: string;

  @IsString()
  @IsNotEmpty()
  provider: Provider;

  constructor(email: string, name: string, provider: Provider, id?: string | number) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.provider = provider;
  }

  static toEntity(request: SocialLoginRequest): Prisma.UserCreateInput {
    return {
      email: request.email,
      password: '',
      name: request.name,
      provider: request.provider,
    };
  }
}
