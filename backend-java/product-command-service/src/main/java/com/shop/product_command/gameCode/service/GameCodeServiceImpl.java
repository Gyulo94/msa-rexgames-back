package com.shop.product_command.gameCode.service;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import com.shop.product_command.gameCode.entity.GameCode;
import com.shop.product_command.gameCode.repository.GameCodeRepository;
import com.shop.product_command.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameCodeServiceImpl implements GameCodeService {

  private final GameCodeRepository gameCodeRepository;

  @Override
  @Transactional
  public void generateGameCodesForProduct(Product product, int stock) {
    if (stock <= 0) {
      return;
    }

    List<GameCode> gameCodes = new ArrayList<>();
    for (int i = 0; i < stock; i++) {
      gameCodes.add(GameCode.builder()
          .product(product)
          .code(generateGameCode())
          .isSold(false)
          .build());
    }
    gameCodeRepository.saveAll(gameCodes);
  }

  @Override
  @Transactional
  public void issueGameCodes(Integer productId, Integer userId, int quantity) {
    List<GameCode> unsoldCodes = gameCodeRepository.findByProductIdAndIsSoldFalse(
        productId,
        PageRequest.of(0, quantity)
    );

    if (unsoldCodes.size() < quantity) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "발급할 게임 코드가 부족합니다. (productId: " + productId + ")");
    }

    for (GameCode code : unsoldCodes) {
      code.markAsSold(userId);
    }
    gameCodeRepository.saveAll(unsoldCodes);
  }

  @Override
  @Transactional
  public void removeUnsoldGameCodes(Integer productId, int quantity) {
    if (quantity <= 0) {
      return;
    }

    List<GameCode> unsoldCodes = gameCodeRepository.findByProductIdAndIsSoldFalse(
        productId,
        PageRequest.of(0, quantity)
    );

    if (unsoldCodes.size() < quantity) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "폐기할 수 있는 미판매 게임 코드가 부족합니다. (요청: " + quantity + ", 남음: " + unsoldCodes.size() + ")");
    }

    gameCodeRepository.deleteAll(unsoldCodes);
  }

  private String generateGameCode() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
      }
      if (i < 3) {
        sb.append("-");
      }
    }
    return sb.toString();
  }
}
