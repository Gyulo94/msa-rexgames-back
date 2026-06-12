package com.shop.order_command.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.order_command.entity.QProduct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public int restoreStock(Integer productId, Integer quantity) {
        QProduct product = QProduct.product;
        return (int) queryFactory.update(product)
                .set(product.stock, product.stock.add(quantity))
                .where(product.id.eq(productId))
                .execute();
    }
}
