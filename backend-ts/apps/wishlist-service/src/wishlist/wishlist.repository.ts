import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common';
import { ProductEvent } from '@app/global';

@Injectable()
export class WishlistRepository {
  constructor(private readonly prisma: PrismaService) {}

  async syncReadOnlyProduct(event: ProductEvent) {
    if (event.name === undefined) {
      await this.prisma.readOnlyProduct.updateMany({
        where: { id: event.productId },
        data: { isDeleted: true },
      });
      return;
    }

    const image =
      event.images && event.images.length > 0 ? event.images[0] : null;

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
}
