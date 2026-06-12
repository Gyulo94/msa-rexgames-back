import { Injectable } from '@nestjs/common';
import { CartRepository } from './cart.repository';
import { ProductEvent } from '@app/global';
import { CartItemRequest } from './request/cart-item.request';
import { ApiException, CartErrorCode } from '../common';
import { CartItemResponse } from './response/cart-item.response';

@Injectable()
export class CartService {
  constructor(private readonly cartRepository: CartRepository) {}

  async syncReadOnlyProduct(event: ProductEvent): Promise<void> {
    await this.cartRepository.syncReadOnlyProduct(event);
  }

  async addToCart(request: CartItemRequest, userId: number): Promise<void> {
    const product = await this.cartRepository.findProductById(
      request.productId,
    );
    if (!product || product.isDeleted) {
      throw new ApiException(CartErrorCode.PRODUCT_NOT_FOUND);
    }
    await this.cartRepository.addToCart(
      request.productId,
      userId,
      request.quantity,
    );
  }
  async updateQuantity(
    request: CartItemRequest,
    userId: number,
  ): Promise<void> {
    if (request.quantity <= 0) {
      await this.cartRepository.removeItem(request.productId, userId);
      return;
    }
    const product = await this.cartRepository.findProductById(
      request.productId,
    );
    if (!product || product.isDeleted) {
      throw new ApiException(CartErrorCode.PRODUCT_NOT_FOUND);
    }
    await this.cartRepository.updateCartItemQuantity(
      request.productId,
      userId,
      request.quantity,
    );
  }

  async removeItem(productId: number, userId: number): Promise<void> {
    await this.cartRepository.removeItem(productId, userId);
  }

  async getCart(userId: number): Promise<CartItemResponse[]> {
    const cacheItems = await this.cartRepository.getCartItemsFromCache(userId);

    const items =
      cacheItems && Object.keys(cacheItems).length > 0
        ? this.parseCacheItems(cacheItems)
        : await this.loadAndCacheFromDb(userId);

    if (items.length === 0) {
      return [];
    }

    return this.enrichCartItems(items, userId);
  }

  private parseCacheItems(
    cacheItems: Record<string, string>,
  ): Array<{ productId: number; quantity: number }> {
    return Object.entries(cacheItems).map(([prodId, qty]) => ({
      productId: parseInt(prodId, 10),
      quantity: parseInt(qty, 10),
    }));
  }

  private async loadAndCacheFromDb(
    userId: number,
  ): Promise<Array<{ productId: number; quantity: number }>> {
    const dbItems = await this.cartRepository.getCartItemsFromDb(userId);
    if (!dbItems || dbItems.length === 0) {
      return [];
    }

    const items = dbItems.map((item) => ({
      productId: item.productId,
      quantity: item.quantity,
    }));

    await this.cartRepository.setCartItemsToCache(userId, items);
    return items;
  }

  private async enrichCartItems(
    items: Array<{ productId: number; quantity: number }>,
    userId: number,
  ): Promise<CartItemResponse[]> {
    const enriched = await Promise.all(
      items.map(async (item) => {
        const product = await this.cartRepository.findProductById(
          item.productId,
        );
        if (!product || product.isDeleted) {
          await this.removeItem(item.productId, userId);
          return null;
        }
        return {
          productId: item.productId,
          quantity: item.quantity,
          product,
        };
      }),
    );
    return enriched.filter((item) => item !== null) as CartItemResponse[];
  }

  async removeItems(userId: number, productIds: number[]): Promise<void> {
    await this.cartRepository.removeItems(userId, productIds);
  }
}
