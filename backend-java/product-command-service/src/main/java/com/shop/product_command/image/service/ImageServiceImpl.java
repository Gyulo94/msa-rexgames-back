package com.shop.product_command.image.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import com.shop.product_command.image.request.ImageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final WebClient webClient;

  @Override
  public List<String> createImage(String id, List<String> images, String entity) {

    var response = webClient.post()
        .uri("/images/rexgames/create")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new ImageRequest(id, List.of(), images, entity))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        })
        .block();

    if (response == null) {
      throw new ApiException(ErrorCode.SAVE_IMAGE_FAILED);
    }

    if (response != null) {
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.get("body");

      if (body != null) {
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) body.get("images");

        log.info("업로드된 이미지 URL들: {}", imageUrls);
        return imageUrls != null ? imageUrls : List.of();
      }
    }

    return List.of();
  }

  @Override
  public List<String> updateImage(String id, List<String> images, List<String> existingImages, String entity) {
    log.info("=== 이미지 업데이트 시작 ===");
    log.info("Entity ID: {}, Type: {}", id, entity);
    log.info("새 이미지 개수: {}, 기존 이미지 개수: {}", images.size(), existingImages.size());

    try {
      var response = webClient.put()
          .uri("/images/rexgames/update")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(new ImageRequest(id, existingImages, images, entity))
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
          })
          .block();

      if (response == null) {
        throw new ApiException(ErrorCode.SAVE_IMAGE_FAILED);
      }

      log.info("Express 서버 응답: {}", response);
      log.info("새 이미지 {} 개 저장 완료", response.size());

      if (response != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.get("body");

        if (body != null) {
          @SuppressWarnings("unchecked")
          List<String> imageUrls = (List<String>) body.get("images");

          log.info("업로드된 이미지 URL들: {}", imageUrls);
          return imageUrls != null ? imageUrls : List.of();
        }
      }
      return List.of();

    } catch (Exception e) {
      log.error("이미지 업데이트 실패: {}", e.getMessage(), e);
      throw new ApiException(ErrorCode.SAVE_IMAGE_FAILED);
    }
  }

  @Override
  public void deleteImages(List<String> ids, String entity) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    for (String id : ids) {
      Map<String, Object> requestBody = Map.of(
          "ids", List.of(id),
          "entity", entity);
      try {
        webClient.method(HttpMethod.DELETE)
            .uri("/images/rexgames/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            })
            .block();
      } catch (WebClientResponseException.NotFound e) {
        log.warn("삭제 대상 이미지가 없어 스킵. entity: {}, id: {}", entity, id);
      } catch (Exception e) {
        log.error("이미지 삭제 실패(스킵): entity: {}, id: {}, message: {}", entity, id, e.getMessage(), e);
      }
    }
  }
}
