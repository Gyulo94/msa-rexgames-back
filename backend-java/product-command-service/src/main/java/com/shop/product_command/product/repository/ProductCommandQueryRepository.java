package com.shop.product_command.product.repository;

import com.shop.product_command.product.entity.Product;
import com.shop.product_command.product.request.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCommandQueryRepository {
  Page<Product> findAll(ProductSearchRequest request, Pageable pageable);
}
