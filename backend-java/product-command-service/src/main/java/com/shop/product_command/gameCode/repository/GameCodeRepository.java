package com.shop.product_command.gameCode.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shop.product_command.gameCode.entity.GameCode;

@Repository
public interface GameCodeRepository extends JpaRepository<GameCode, Integer> {
    List<GameCode> findByProductIdAndIsSoldFalse(Integer productId, Pageable pageable);
}
