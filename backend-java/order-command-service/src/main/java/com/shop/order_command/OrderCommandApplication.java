package com.shop.order_command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shop")
public class OrderCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderCommandApplication.class, args);
    }
}
