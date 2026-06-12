package com.shop.product_command.genre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shop.product_command.genre.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
}
