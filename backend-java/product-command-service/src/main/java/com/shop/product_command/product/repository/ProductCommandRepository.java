package com.shop.product_command.product.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.shop.product_command.product.entity.Product;

@Repository
public interface ProductCommandRepository extends JpaRepository<Product, Integer>, ProductCommandQueryRepository {

}
