import { Controller } from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import type { ProductEvent } from '@app/global';
import { WishlistService } from './wishlist.service';

@Controller('wishlist')
export class WishlistController {
  constructor(private readonly wishlistService: WishlistService) {}

  @EventPattern('product-topic')
  async handleProductCreated(@Payload() event: ProductEvent) {
    await this.wishlistService.syncReadOnlyProduct(event);
  }
}
