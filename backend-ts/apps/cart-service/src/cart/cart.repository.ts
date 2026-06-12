import { Inject, Injectable } from '@nestjs/common';
import { PrismaService } from '../common';
import { ProductEvent, RedisKey } from '@app/global';
import * as Redis from 'ioredis';

@Injectable()
export class CartRepository {
  private static readonly CART_TTL = 30 * 24 * 60 * 60;

  constructor(
    private readonly prisma: PrismaService,
    @Inject('Redis') private readonly redis: Redis.Redis,
  ) {}

  async syncReadOnlyProduct(event: ProductEvent) {
    if (event.name === undefined) {
      await this.prisma.readOnlyProduct.updateMany({
        where: { id: event.productId },
        data: { isDeleted: true },
      });
      return;
    }

    const image = event.images?.[0] ?? null;

    await this.prisma.readOnlyProduct.upsert({
      where: { id: event.productId },
      update: {
        name: event.name,
        price: event.price,
        discount: event.discount,
        discountPrice: event.discountPrice,
        stock: event.stock,
        image,
        isDeleted: false,
      },
      create: {
        id: event.productId,
        name: event.name,
        price: event.price,
        discount: event.discount,
        discountPrice: event.discountPrice,
        stock: event.stock,
        image,
        isDeleted: false,
      },
    });
  }

  async findProductById(productId: number) {
    return this.prisma.readOnlyProduct.findUnique({
      where: { id: productId },
    });
  }

  async getOrCreateCart(userId: number) {
    return this.prisma.cart.upsert({
      where: { userId },
      update: {},
      create: { userId },
    });
  }

  async addToCart(productId: number, userId: number, quantity: number) {
    const cart = await this.getOrCreateCart(userId);
    const redisKey = RedisKey.cachedCart(userId);
    const productStr = productId.toString();

    const redisPromise = this.redis
      .pipeline()
      .hincrby(redisKey, productStr, quantity)
      .expire(redisKey, CartRepository.CART_TTL)
      .exec();

    const dbPromise = this.prisma.cartItem.upsert({
      where: {
        cartId_productId: {
          cartId: cart.id,
          productId,
        },
      },
      update: {
        quantity: {
          increment: quantity,
        },
      },
      create: {
        cartId: cart.id,
        productId,
        quantity,
      },
    });

    await Promise.all([redisPromise, dbPromise]);
  }

  async updateCartItemQuantity(
    productId: number,
    userId: number,
    quantity: number,
  ) {
    const cart = await this.getOrCreateCart(userId);
    const redisKey = RedisKey.cachedCart(userId);
    const productStr = productId.toString();

    const redisPromise = this.redis
      .pipeline()
      .hset(redisKey, productStr, quantity.toString())
      .expire(redisKey, CartRepository.CART_TTL)
      .exec();

    const dbPromise = this.prisma.cartItem.upsert({
      where: {
        cartId_productId: {
          cartId: cart.id,
          productId,
        },
      },
      update: {
        quantity,
      },
      create: {
        cartId: cart.id,
        productId,
        quantity,
      },
    });

    await Promise.all([redisPromise, dbPromise]);
  }

  async removeItem(productId: number, userId: number) {
    const cart = await this.getOrCreateCart(userId);
    const redisKey = RedisKey.cachedCart(userId);
    const productStr = productId.toString();

    await Promise.all([
      this.redis.hdel(redisKey, productStr),
      this.prisma.cartItem.deleteMany({
        where: {
          cartId: cart.id,
          productId,
        },
      }),
    ]);
  }

  async getCartItemsFromCache(userId: number): Promise<Record<string, string>> {
    return this.redis.hgetall(RedisKey.cachedCart(userId));
  }

  async getCartItemsFromDb(userId: number) {
    const cart = await this.prisma.cart.findUnique({
      where: { userId },
      include: {
        items: true,
      },
    });
    return cart ? cart.items : [];
  }

  async setCartItemsToCache(
    userId: number,
    items: Array<{ productId: number; quantity: number }>,
  ) {
    const redisKey = RedisKey.cachedCart(userId);
    if (items.length === 0) {
      await this.redis.del(redisKey);
      return;
    }

    const hashData: Record<string, string> = {};
    for (const item of items) {
      hashData[item.productId.toString()] = item.quantity.toString();
    }

    await this.redis
      .pipeline()
      .hset(redisKey, hashData)
      .expire(redisKey, CartRepository.CART_TTL)
      .exec();
  }

  async removeItems(userId: number, productIds: number[]) {
    if (!productIds || productIds.length === 0) return;
    const cart = await this.getOrCreateCart(userId);
    const redisKey = RedisKey.cachedCart(userId);
    const productStrs = productIds.map((id) => id.toString());

    await Promise.all([
      this.redis.hdel(redisKey, ...productStrs),
      this.prisma.cartItem.deleteMany({
        where: {
          cartId: cart.id,
          productId: { in: productIds },
        },
      }),
    ]);
  }
}
