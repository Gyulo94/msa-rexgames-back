import { ProductQueryRepository } from '../repository/product-query.repository';
import { redis, ApiException, RedisKey, ErrorCode, LOGGER } from '@/common';
import { Prisma } from '@prisma/client-query';
import { Request } from 'express';
import { ProductFilterRequest } from '../schema/prodict-filter.schema';
import { ProductListResponse } from '../response/product-list.response';

export class ProductQueryService {
  private readonly productQueryRepository: ProductQueryRepository;

  constructor() {
    this.productQueryRepository = new ProductQueryRepository();
  }

  async findAll(req: Request) {
    const query = (req.validated || {}) as ProductFilterRequest;
    const {
      minPrice,
      maxPrice,
      name,
      genres,
      platforms,
      sortBy,
      sortOrder,
      hasDiscount,
    } = query;
    const page = query.page ?? 1;
    const take = query.take ?? 10;

    const filterState = JSON.stringify({
      genres,
      platforms,
      minPrice,
      maxPrice,
      page,
      take,
      sortBy,
      sortOrder,
      name,
      hasDiscount,
    });

    const redisKey = RedisKey.productSearch(filterState);

    const cached = await redis.get(redisKey);
    if (cached) {
      LOGGER.info('필터 검색 결과 - Redis에서 즉시 반환');
      return JSON.parse(cached);
    }

    LOGGER.info('전체 목록 - DB 조회 발생');

    const skip = (page - 1) * take;
    let where: Prisma.ProductQueryWhereInput = {
      isDeleted: false,
    };

    if (genres && genres.length > 0) {
      where.genreName = { in: genres as string[] };
    }

    if (platforms && platforms.length > 0) {
      where.platformName = { in: platforms as string[] };
    }

    if (minPrice !== undefined || maxPrice !== undefined) {
      where.price = {};
      if (minPrice !== undefined) where.price.gte = minPrice;
      if (maxPrice !== undefined) where.price.lte = maxPrice;
    }

    if (name) {
      where.name = {
        contains: name as string,
        mode: 'insensitive',
      };
    }

    if (hasDiscount === 'true') {
      where.discount = {
        gt: 0,
      };
    }

    const orderBy: Record<string, string> = {};
    orderBy[sortBy || 'createdAt'] = sortOrder || 'desc';

    const [products, totalCount] = await Promise.all([
      this.productQueryRepository.findAll({ where, take, skip, orderBy }),
      this.productQueryRepository.count(where),
    ]);

    const response: ProductListResponse = {
      products,
      totalCount,
      currentPage: page,
      totalPages: Math.ceil(totalCount / take),
    };

    await redis.setEx(redisKey, 300, JSON.stringify(response));

    return response;
  }

  async findById(productId: number) {
    const redisKey = RedisKey.productDetail(productId);
    const cached = await redis.get(redisKey);
    if (cached) {
      LOGGER.info('상품 상세 - Redis에서 즉시 반환');
      return JSON.parse(cached);
    }

    LOGGER.info('상품 상세 - DB 조회 발생');

    const product = await this.productQueryRepository.findById(productId);
    if (!product || product.isDeleted) {
      throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    await redis.setEx(redisKey, 300, JSON.stringify(product));

    return product;
  }
}
