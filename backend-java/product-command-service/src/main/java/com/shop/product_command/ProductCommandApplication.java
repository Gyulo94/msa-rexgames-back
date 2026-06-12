package com.shop.product_command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shop")
public class ProductCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductCommandApplication.class, args);
    }
}
