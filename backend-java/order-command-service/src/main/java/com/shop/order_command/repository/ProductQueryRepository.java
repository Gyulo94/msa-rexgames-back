package com.shop.order_command.repository;

public interface ProductQueryRepository {
    int restoreStock(Integer productId, Integer quantity);
}
