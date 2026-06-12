package com.shop.product_command.product.service;

import com.shop.product_command.product.request.ProductRequest;
import com.shop.product_command.product.request.ProductSearchRequest;
import com.shop.product_command.product.response.ProductResponse;
import com.shop.common.event.OrderCompletedEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  ProductResponse createProduct(ProductRequest request);

  Page<ProductResponse> findAll(ProductSearchRequest request, Pageable pageable);

  ProductResponse findById(int id);

  ProductResponse updateProduct(ProductRequest request, int id);

  void deleteProducts(int[] ids);

  void deductStock(int id, int quantity);

  void processOrderCompletion(OrderCompletedEvent event);
}
