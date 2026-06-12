import { User } from '@prisma/client-auth';
import { TokenResponse } from './token.response';

export class AuthResponse {
  user: Omit<User, 'password'>;
  serverTokens: TokenResponse;

  constructor(user: Omit<User, 'password'>, token: TokenResponse) {
    this.user = user;
    this.serverTokens = token;
  }

  static fromEntity(user: Omit<User, 'password'>, serverTokens: TokenResponse) {
    return new AuthResponse(user, serverTokens);
  }
}
