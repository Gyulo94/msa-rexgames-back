package com.shop.product_command.image.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageRequest {

  private String id;
  private List<String> existingImages;
  private List<String> images;
  private String entity;
}
