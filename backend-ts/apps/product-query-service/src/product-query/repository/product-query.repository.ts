import { ProductEvent } from '@/common';
import PrismaService from '@/prisma';
import { PrismaClient, Prisma } from '@prisma/client-query';

export class ProductQueryRepository {
  private readonly prisma: PrismaClient;
  constructor() {
    this.prisma = PrismaService.getInstance();
  }

  findAll(filter: {
    where: Prisma.ProductQueryWhereInput;
    take: number;
    skip: number;
    orderBy: Record<string, string>;
  }) {
    const { where, take, skip, orderBy } = filter;

    return this.prisma.productQuery.findMany({
      where,
      orderBy,
      skip,
      take,
    });
  }

  count(where: Prisma.ProductQueryWhereInput) {
    return this.prisma.productQuery.count({
      where,
    });
  }

  findById(productId: number) {
    return this.prisma.productQuery.findUnique({
      where: { productId },
    });
  }

  upsertProduct(event: ProductEvent) {
    return this.prisma.productQuery.upsert({
      where: { productId: event.productId },
      update: {
        name: event.name,
        slug: event.slug,
        price: event.price,
        discount: event.discount,
        discountPrice: event.discountPrice,
        genreName: event.genreName,
        platformName: event.platformName,
        description: event.description,
        reviewRating: event.reviewRating,
        reviewCount: event.reviewCount,
        stock: event.stock,
        specs: event.specs,
        images: event.images,
        isDeleted: false,
      },
      create: {
        productId: event.productId,
        name: event.name,
        slug: event.slug,
        price: event.price,
        discount: event.discount,
        discountPrice: event.discountPrice,
        genreName: event.genreName,
        platformName: event.platformName,
        description: event.description,
        reviewRating: event.reviewRating,
        reviewCount: event.reviewCount,
        stock: event.stock,
        specs: event.specs,
        images: event.images,
        isDeleted: false,
      },
    });
  }

  deleteProduct(productId: number) {
    return this.prisma.productQuery.updateMany({
      where: { productId },
      data: { isDeleted: true },
    });
  }
}
