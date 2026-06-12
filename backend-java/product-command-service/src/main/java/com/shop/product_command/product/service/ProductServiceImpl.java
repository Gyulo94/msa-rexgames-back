package com.shop.product_command.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;

import com.shop.product_command.platform.repository.PlatformRepository;
import com.shop.product_command.image.service.ImageService;
import com.shop.product_command.platform.entity.Platform;
import com.shop.common.event.ProductCreatedEvent;
import com.shop.common.event.ProductUpdatedEvent;
import com.shop.common.event.ProductDeletedEvent;
import com.shop.common.event.OrderCompletedEvent;
import com.shop.common.exception.ApiException;
import com.shop.product_command.genre.repository.GenreRepository;
import com.shop.product_command.genre.entity.Genre;
import com.shop.common.error.ErrorCode;
import com.shop.product_command.product.entity.Product;
import com.shop.product_command.gameCode.service.GameCodeService;
import lombok.RequiredArgsConstructor;
import com.shop.product_command.product.request.ProductRequest;
import com.shop.product_command.product.request.ProductSearchRequest;
import com.shop.product_command.product.response.ProductResponse;
import com.shop.product_command.product.repository.ProductCommandRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductCommandRepository productCommandRepository;
  private final GenreRepository genreRepository;
  private final PlatformRepository platformRepository;
  private final ImageService imageService;
  private final ApplicationEventPublisher eventPublisher;
  private final GameCodeService gameCodeService;

  @Override
  @Transactional
  public ProductResponse createProduct(ProductRequest request) {
    Genre genre = genreRepository.findById(request.getGenreId())
        .orElseThrow(() -> new ApiException(ErrorCode.GENRE_NOT_FOUND));
    Platform platform = platformRepository.findById(request.getPlatformId())
        .orElseThrow(() -> new ApiException(ErrorCode.PLATFORM_NOT_FOUND));

    Product product = ProductRequest.toEntity(request);
    Product savedProduct = productCommandRepository.save(product);

    if (request.getImages() != null && !request.getImages().isEmpty()) {
      List<String> savedImages = imageService.createImage(
          savedProduct.getId().toString(),
          request.getImages(),
          "products");
      savedProduct.updateImages(savedImages);
    }

    processTempImagesInDescription(savedProduct);

    gameCodeService.generateGameCodesForProduct(savedProduct, savedProduct.getStock());

    ProductCreatedEvent event = ProductCreatedEvent.builder()
        .productId(savedProduct.getId())
        .name(savedProduct.getName())
        .slug(savedProduct.getSlug())
        .price(savedProduct.getPrice())
        .discount(savedProduct.getDiscount())
        .discountPrice(savedProduct.getDiscountPrice())
        .genreName(genre.getName())
        .platformName(platform.getName())
        .description(savedProduct.getDescription())
        .reviewRating(savedProduct.getReviewRating())
        .reviewCount(savedProduct.getReviewCount())
        .stock(savedProduct.getStock())
        .specs(savedProduct.getSpecs())
        .images(savedProduct.getImages())
        .build();

    eventPublisher.publishEvent(event);

    ProductResponse response = ProductResponse.fromEntity(savedProduct);
    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> findAll(ProductSearchRequest request, Pageable pageable) {
    Page<Product> products = productCommandRepository.findAll(request, pageable);
    return products.map(ProductResponse::fromEntity);
  }

  @Override
  public ProductResponse findById(int id) {
    Product product = productCommandRepository.findById(id)
        .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
    if (Boolean.TRUE.equals(product.getIsDeleted())) {
      throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    ProductResponse response = ProductResponse.fromEntity(product);
    return response;
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(ProductRequest request, int id) {
    Product product = productCommandRepository.findById(id)
        .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
    if (Boolean.TRUE.equals(product.getIsDeleted())) {
      throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    Genre genre = genreRepository.findById(request.getGenreId())
        .orElseThrow(() -> new ApiException(ErrorCode.GENRE_NOT_FOUND));
    Platform platform = platformRepository.findById(request.getPlatformId())
        .orElseThrow(() -> new ApiException(ErrorCode.PLATFORM_NOT_FOUND));

    int oldStock = product.getStock();

    List<String> oldDescImages = extractProductImageUrls(product.getDescription(), product.getId());
    List<String> oldMainImages = product.getImages() != null ? product.getImages() : List.of();
    List<String> oldAllImages = new ArrayList<>();
    oldAllImages.addAll(oldMainImages);
    oldAllImages.addAll(oldDescImages);

    product.update(ProductRequest.toEntity(request));
    product.updateDescription(request.getDescription());

    List<String> newlyCreatedDescImages = processTempImagesInDescription(product);
    oldAllImages.addAll(newlyCreatedDescImages);

    List<String> newDescImages = extractProductImageUrls(product.getDescription(), product.getId());

    List<String> requestMainImages = request.getImages() != null ? request.getImages() : List.of();
    List<String> desiredImages = new ArrayList<>();
    desiredImages.addAll(requestMainImages);
    desiredImages.addAll(newDescImages);

    List<String> updatedAllImages = imageService.updateImage(
        product.getId().toString(),
        desiredImages,
        oldAllImages,
        "products");

    List<String> updatedMainImages = new ArrayList<>(updatedAllImages.subList(0, requestMainImages.size()));
    product.updateImages(updatedMainImages);

    int newStock = product.getStock();
    if (newStock > oldStock) {
      gameCodeService.generateGameCodesForProduct(product, newStock - oldStock);
    } else if (newStock < oldStock) {
      gameCodeService.removeUnsoldGameCodes(product.getId(), oldStock - newStock);
    }

    ProductUpdatedEvent event = ProductUpdatedEvent.builder()
        .productId(product.getId())
        .name(product.getName())
        .slug(product.getSlug())
        .price(product.getPrice())
        .discount(product.getDiscount())
        .discountPrice(product.getDiscountPrice())
        .genreName(genre.getName())
        .platformName(platform.getName())
        .description(product.getDescription())
        .reviewRating(product.getReviewRating())
        .reviewCount(product.getReviewCount())
        .stock(product.getStock())
        .specs(product.getSpecs())
        .images(product.getImages())
        .build();

    eventPublisher.publishEvent(event);

    ProductResponse response = ProductResponse.fromEntity(product);
    return response;
  }

  @Override
  @Transactional
  public void deleteProducts(int[] ids) {
    if (ids == null || ids.length == 0) {
      return;
    }

    List<Product> products = Arrays.stream(ids)
        .mapToObj(id -> productCommandRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND)))
        .toList();

    for (Product product : products) {
      product.delete();
      productCommandRepository.save(product);

      ProductDeletedEvent event = ProductDeletedEvent.builder()
          .productId(product.getId())
          .build();
      eventPublisher.publishEvent(event);
    }
  }

  @Override
  @Transactional
  public void deductStock(int id, int quantity) {
    Product product = productCommandRepository.findById(id)
        .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

    if (product.getStock() < quantity) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "재고가 부족합니다. (productId: " + id + ")");
    }

    product.setStock(product.getStock() - quantity);
    productCommandRepository.save(product);

    ProductUpdatedEvent event = ProductUpdatedEvent.builder()
        .productId(product.getId())
        .name(product.getName())
        .slug(product.getSlug())
        .price(product.getPrice())
        .discount(product.getDiscount())
        .discountPrice(product.getDiscountPrice())
        .genreName(product.getGenre() != null ? product.getGenre().getName() : null)
        .platformName(product.getPlatform() != null ? product.getPlatform().getName() : null)
        .description(product.getDescription())
        .reviewRating(product.getReviewRating())
        .reviewCount(product.getReviewCount())
        .stock(product.getStock())
        .specs(product.getSpecs())
        .images(product.getImages())
        .build();

    eventPublisher.publishEvent(event);
  }

  // 제품 설명에서 이미지 URL을 추출하는 메서드
  private List<String> extractProductImageUrls(String description, int productId) {
    List<String> urls = new ArrayList<>();
    if (description == null || description.isEmpty()) {
      return urls;
    }
    Pattern pattern = Pattern.compile(
        "((?:https?://[^\"'\\s()>]*)?/rexgames/images/products/" + productId + "/[^\"'\\s()>/]+)",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(description);
    while (matcher.find()) {
      urls.add(matcher.group(1));
    }
    return urls.stream().distinct().toList();
  }

  // 제품 설명에서 임시 이미지 URL을 찾아 실제 이미지로 교체하는 메서드
  private List<String> processTempImagesInDescription(Product product) {
    String description = product.getDescription();
    if (description == null || description.isEmpty()) {
      return List.of();
    }

    List<String> tempPaths = new ArrayList<>();
    Pattern tempPattern = Pattern.compile(
        "((?:https?://[^/\\s]+)?/(?:[^\"'\\s()>]*/)?temp\\b(?:/[^\"'\\s()]*)?)",
        Pattern.CASE_INSENSITIVE);
    Matcher matcher = tempPattern.matcher(description);
    while (matcher.find()) {
      tempPaths.add(matcher.group(1));
    }

    if (!tempPaths.isEmpty()) {
      List<String> uniqueTempPaths = tempPaths.stream().distinct().toList();
      List<String> realPaths = imageService.createImage(
          product.getId().toString(),
          uniqueTempPaths,
          "products");
      for (int i = 0; i < Math.min(uniqueTempPaths.size(), realPaths.size()); i++) {
        description = description.replace(uniqueTempPaths.get(i), realPaths.get(i));
      }
      product.updateDescription(description);
      return realPaths;
    }
    return List.of();
  }

  @Override
  @Transactional
  public void processOrderCompletion(OrderCompletedEvent event) {
    for (OrderCompletedEvent.OrderItemInfo item : event.getItems()) {
      this.deductStock(item.getProductId(), item.getQuantity());
      gameCodeService.issueGameCodes(item.getProductId(), event.getUserId(), item.getQuantity());
    }
  }
}
