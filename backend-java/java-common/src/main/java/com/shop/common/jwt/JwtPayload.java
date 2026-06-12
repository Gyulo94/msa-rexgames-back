package com.shop.common.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPayload {
  private Integer id;
  private String role;
}
