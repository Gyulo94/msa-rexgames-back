package com.shop.product_command.product.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shop.product_command.product.request.ProductRequest;
import com.shop.product_command.product.request.ProductSearchRequest;
import com.shop.product_command.product.response.ProductResponse;
import com.shop.product_command.product.service.ProductService;
import com.shop.common.message.ResponseMessage;

import jakarta.validation.Valid;

import com.shop.common.annotations.Admin;
import com.shop.common.api.Api;

import lombok.RequiredArgsConstructor;

@Admin
@RestController
@RequestMapping("product")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @PostMapping("create")
  public Api<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
    ProductResponse response = productService.createProduct(request);
    return Api.OK(response, ResponseMessage.CREATE_PRODUCT_SUCCESS);
  }

  @GetMapping("all")
  public Api<Page<ProductResponse>> findProductsAll(
      ProductSearchRequest request,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<ProductResponse> response = productService.findAll(request, pageable);
    return Api.OK(response);
  }

  @GetMapping("{id}")
  public Api<ProductResponse> findProductById(@PathVariable int id) {
    ProductResponse response = productService.findById(id);
    return Api.OK(response);
  }

  @PutMapping("update/{id}")
  public Api<ProductResponse> updateProduct(
      @PathVariable int id,
      @Valid @RequestBody ProductRequest request) {
    ProductResponse response = productService.updateProduct(request, id);
    return Api.OK(response, ResponseMessage.UPDATE_PRODUCT_SUCCESS);
  }

  @DeleteMapping("delete")
  public Api<Void> deleteProduct(@RequestBody int[] ids) {
    productService.deleteProducts(ids);
    return Api.OK(ResponseMessage.DELETE_PRODUCT_SUCCESS);
  }
}
