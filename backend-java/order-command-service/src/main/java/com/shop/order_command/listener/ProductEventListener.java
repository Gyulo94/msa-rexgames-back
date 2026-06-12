package com.shop.order_command.listener;

import com.shop.common.event.ProductCreatedEvent;
import com.shop.common.event.ProductDeletedEvent;
import com.shop.common.event.ProductUpdatedEvent;
import com.shop.order_command.entity.Product;
import com.shop.order_command.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = "product-topic", groupId = "order-service-consumer")
public class ProductEventListener {

    private final ProductRepository productRepository;

    @KafkaHandler
    public void handleProductCreated(ProductCreatedEvent event) {
        log.debug("ProductCreatedEvent를 처리합니다: productId={}", event.getProductId());
        String image = (event.getImages() != null && !event.getImages().isEmpty()) ? event.getImages().get(0) : null;
        Product product = Product.builder()
                .id(event.getProductId())
                .name(event.getName())
                .price(event.getPrice())
                .discount(event.getDiscount())
                .discountPrice(event.getDiscountPrice())
                .stock(event.getStock())
                .image(image)
                .isDeleted(false)
                .build();
        productRepository.save(product);
    }

    @KafkaHandler
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.debug("ProductUpdatedEvent를 처리합니다: productId={}", event.getProductId());
        Product product = productRepository.findById(event.getProductId())
                .orElse(new Product());

        String image = (event.getImages() != null && !event.getImages().isEmpty()) ? event.getImages().get(0)
                : product.getImage();
        product.setId(event.getProductId());
        product.setName(event.getName());
        product.setPrice(event.getPrice());
        product.setDiscount(event.getDiscount());
        product.setDiscountPrice(event.getDiscountPrice());
        product.setStock(event.getStock());
        product.setImage(image);
        product.setIsDeleted(false);

        productRepository.save(product);
    }

    @KafkaHandler
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.debug("ProductDeletedEvent를 처리합니다: productId={}", event.getProductId());
        productRepository.findById(event.getProductId()).ifPresent(product -> {
            product.setIsDeleted(true);
            productRepository.save(product);
        });
    }
}
