package com.shop.product_command.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shop.product_command.platform.entity.Platform;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {
}
