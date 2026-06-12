package com.shop.common.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.shop.common.constants.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

  private final Constants constants;
  private final SecretKey accessTokenSigningKey;

  public JwtProvider(Constants constants) {
    this.constants = constants;
    this.accessTokenSigningKey = getSigningKey(constants.getAccessTokenSecretKey());
  }

  private SecretKey getSigningKey(String secretKeyBase64) {
    return Keys.hmacShaKeyFor(secretKeyBase64.getBytes(StandardCharsets.UTF_8));
  }

  public Claims validateAccessToken(String accessToken) {
    return parseClaims(accessToken, accessTokenSigningKey, "액세스 토큰");
  }

  private Claims parseClaims(String token, SecretKey signingKey, String tokenType) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 {}입니다.", tokenType, e);
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 {}입니다.", tokenType, e);
    } catch (MalformedJwtException e) {
      log.warn("형식 오류 또는 잘못된 구성의 {}입니다.", tokenType, e);
    } catch (SignatureException e) {
      log.warn("유효하지 않은 {} 서명입니다.", tokenType, e);
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않거나 비어있는 {}입니다.", tokenType, e);
    } catch (Exception e) {
      log.error("{} 검증 중 알 수 없는 오류 발생.", tokenType, e);
    }
    return null;
  }
}
