import {
  Body,
  Controller,
  Post,
  Get,
  Patch,
  Delete,
  Param,
  ParseIntPipe,
} from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import { CartService } from './cart.service';
import {
  CurrentUser,
  Message,
  ResponseMessage,
  type OrderCompletedEvent,
  type JwtPayload,
  type ProductEvent,
} from '@app/global';
import { CartItemRequest } from './request/cart-item.request';

@Controller('cart')
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @EventPattern('product-topic')
  async handleProductCreated(@Payload() event: ProductEvent) {
    await this.cartService.syncReadOnlyProduct(event);
  }

  @EventPattern('order-completed')
  async handleOrderCompleted(@Payload() event: OrderCompletedEvent) {
    const productIds = event.items.map((item) => item.productId);
    await this.cartService.removeItems(event.userId, productIds);
  }

  @Get()
  async getCart(@CurrentUser() user: JwtPayload) {
    return await this.cartService.getCart(user.id);
  }

  @Message(ResponseMessage.CART_ITEM_ADDED)
  @Post('add')
  async addToCart(
    @Body() request: CartItemRequest,
    @CurrentUser() user: JwtPayload,
  ) {
    await this.cartService.addToCart(request, user.id);
  }

  @Patch('quantity')
  async updateQuantity(
    @Body() request: CartItemRequest,
    @CurrentUser() user: JwtPayload,
  ) {
    await this.cartService.updateQuantity(request, user.id);
  }

  @Message(ResponseMessage.CART_ITEM_REMOVED)
  @Delete('items/:productId')
  async removeItem(
    @Param('productId', ParseIntPipe) productId: number,
    @CurrentUser() user: JwtPayload,
  ) {
    await this.cartService.removeItem(productId, user.id);
  }
}
