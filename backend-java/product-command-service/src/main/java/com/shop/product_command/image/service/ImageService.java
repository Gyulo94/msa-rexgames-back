package com.shop.product_command.image.service;

import java.util.List;

public interface ImageService {

  List<String> createImage(String id, List<String> images, String entity);

  List<String> updateImage(String id, List<String> images, List<String> existingImages, String entity);

  void deleteImages(List<String> ids, String entity);

}
