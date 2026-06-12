package com.shop.product_command.gameCode.service;

import com.shop.product_command.product.entity.Product;

public interface GameCodeService {
  void generateGameCodesForProduct(Product product, int stock);
  void issueGameCodes(Integer productId, Integer userId, int quantity);
  void removeUnsoldGameCodes(Integer productId, int quantity);
}
