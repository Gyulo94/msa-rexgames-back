import { HttpStatus } from '@nestjs/common';

export enum WishlistErrorCode {}

export const WishlistErrorCodeMap: Record<
  WishlistErrorCode,
  { status: HttpStatus; message: string }
> = {};
