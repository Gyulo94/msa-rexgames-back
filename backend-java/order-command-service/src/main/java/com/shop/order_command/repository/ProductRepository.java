package com.shop.order_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.order_command.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, ProductQueryRepository {
}
