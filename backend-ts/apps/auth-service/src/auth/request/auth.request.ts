import { IsEmail, IsNotEmpty, MinLength } from 'class-validator';

export class AuthRequest {
  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsNotEmpty()
  @MinLength(8)
  password: string;

  constructor(email: string, password: string) {
    this.email = email;
    this.password = password;
  }
}
