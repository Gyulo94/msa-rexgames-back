import {
  Body,
  Controller,
  Get,
  Post,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { CreateUserRequest } from '../user/request/create-user.request';
import {
  CurrentUser,
  Message,
  Public,
  RefreshJwtGuard,
  ResponseMessage,
} from '@app/global';
import type { JwtPayload } from '@app/global';
import { AuthRequest } from './request/auth.request';
import { TokenResponse } from './response/token.response';
import { ResetPasswordRequest } from './request/reset-password.request';
import { SocialLoginRequest } from './request/social-login.request';
import { AuthResponse } from './response/auth.response';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Message(ResponseMessage.VERIFICATION_EMAIL_SENT)
  @Post('register')
  async register(@Body() request: CreateUserRequest) {
    const response = await this.authService.register(request);
    return response;
  }

  @Public()
  @Message(ResponseMessage.VERIFICATION_SUCCESS)
  @Get('verify')
  async verify(@Query() request: { token: string; type: string }) {
    const response = await this.authService.verify(request.token, request.type);
    return response;
  }

  @Public()
  @Post('login')
  async login(@Body() request: AuthRequest) {
    const response = await this.authService.login(request);
    return response;
  }

  @Public()
  @Post('admin-login')
  async adminLogin(@Body() request: AuthRequest) {
    const response = await this.authService.adminLogin(request);
    return response;
  }

  @Public()
  @UseGuards(RefreshJwtGuard)
  @Post('refresh')
  async refresh(
    @Body('oldRefreshToken') oldRefreshToken: string,
    @CurrentUser() user: JwtPayload,
  ): Promise<TokenResponse> {
    const response: TokenResponse = await this.authService.refresh(
      user,
      oldRefreshToken,
    );
    return response;
  }

  @Public()
  @Post('social-login')
  async socialLogin(
    @Body() request: SocialLoginRequest,
  ): Promise<AuthResponse> {
    const response: AuthResponse = await this.authService.socialLogin(request);
    return response;
  }

  @Post('logout')
  async logout(@CurrentUser() user: JwtPayload) {
    await this.authService.logout(+user.id);
  }

  @Public()
  @Message(ResponseMessage.VERIFICATION_EMAIL_SENT)
  @Post('reset-password/send')
  async sendResetPasswordMail(@Body('email') email: string) {
    await this.authService.sendResetPasswordMail(email);
  }

  @Public()
  @Message(ResponseMessage.VERIFICATION_SUCCESS)
  @Get('reset-password/verify')
  async verifyResetPasswordToken(@Query('token') token: string) {
    const response = await this.authService.verify(token, 'reset');
    return response;
  }

  @Public()
  @Message(ResponseMessage.PASSWORD_RESET_SUCCESS)
  @Put('reset-password')
  async resetPassword(@Body() request: ResetPasswordRequest) {
    const response = await this.authService.resetPassword(request);
    return response;
  }
}
