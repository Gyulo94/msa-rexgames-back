import { ProductCreatedEvent } from '@app/global';
import { Injectable } from '@nestjs/common';
import { WishlistRepository } from './wishlist.repository';

@Injectable()
export class WishlistService {
  constructor(private readonly wishlistRepository: WishlistRepository) {}

  async syncReadOnlyProduct(event: ProductCreatedEvent) {
    await this.wishlistRepository.syncReadOnlyProduct(event);
  }
}
