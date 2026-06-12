package com.shop.product_command.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.product_command.product.entity.Product;
import com.shop.product_command.product.request.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.shop.product_command.product.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductCommandQueryRepositoryImpl implements ProductCommandQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Product> findAll(ProductSearchRequest request, Pageable pageable) {
    BooleanExpression[] conditions = {
        product.isDeleted.eq(false),
        StringUtils.hasText(request.getName()) ? product.name.containsIgnoreCase(request.getName()) : null,
        !CollectionUtils.isEmpty(request.getGenres()) ? product.genre.name.in(request.getGenres()) : null,
        !CollectionUtils.isEmpty(request.getPlatforms()) ? product.platform.name.in(request.getPlatforms()) : null,
        request.getMinPrice() != null ? product.discountPrice.goe(request.getMinPrice()) : null,
        request.getMaxPrice() != null ? product.discountPrice.loe(request.getMaxPrice()) : null,
        request.getMinDiscount() != null ? product.discount.goe(request.getMinDiscount()) : null,
        request.getMaxDiscount() != null ? product.discount.loe(request.getMaxDiscount()) : null,
        request.getMinRating() != null ? product.reviewRating.goe(request.getMinRating()) : null
    };

    List<Product> products = queryFactory
        .selectFrom(product)
        .leftJoin(product.genre).fetchJoin()
        .leftJoin(product.platform).fetchJoin()
        .where(conditions)
        .orderBy(getOrderSpecifier(request.getSortBy(), request.getSortOrder()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long totalCount = queryFactory
        .select(product.count())
        .from(product)
        .where(conditions)
        .fetchOne();
    long total = totalCount != null ? totalCount : 0L;

    return new PageImpl<>(products, pageable, total);
  }

  private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortOrder) {
    if (sortBy == null || sortBy.isEmpty()) {
      return product.id.desc();
    }

    boolean isDesc = "desc".equalsIgnoreCase(sortOrder);
    return switch (sortBy) {
      case "name" -> isDesc ? product.name.desc() : product.name.asc();
      case "genreName" -> isDesc ? product.genre.name.desc() : product.genre.name.asc();
      case "platform" -> isDesc ? product.platform.name.desc() : product.platform.name.asc();
      case "discountPrice" -> isDesc ? product.discountPrice.desc() : product.discountPrice.asc();
      case "discount" -> isDesc ? product.discount.desc() : product.discount.asc();
      case "rating" -> isDesc ? product.reviewRating.desc() : product.reviewRating.asc();
      default -> product.id.desc();
    };
  }
}
