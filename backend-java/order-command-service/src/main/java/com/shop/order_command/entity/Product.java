package com.shop.order_command.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private Integer id;

    private String name;

    private Integer price;

    private Integer discount;

    private Integer discountPrice;

    private Integer stock;

    private String image;

    private Boolean isDeleted;
}
