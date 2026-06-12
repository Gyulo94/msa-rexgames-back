package com.shop.common.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String accessToken = getAccessTokenFromHeader(request);

      if (accessToken == null) {
        log.debug("헤더에 액세스 토큰이 없습니다. URI: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
        return;
      }

      Claims claims = jwtProvider.validateAccessToken(accessToken);

      if (claims == null) {
        log.warn("유효하지 않은 액세스 토큰입니다. URI: {}", request.getRequestURI());
        throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
      }

      Integer userId = null;
      Number idClaim = claims.get("id", Number.class);
      if (idClaim != null) {
        userId = idClaim.intValue();
      } else {
        String subject = claims.getSubject();
        if (subject != null) {
          try {
            userId = Integer.valueOf(subject);
          } catch (NumberFormatException ignored) {
          }
        }
      }

      if (userId == null) {
        throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
      }

      String role = claims.get("role", String.class);
      java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = null;
      if (role != null) {
        authorities = java.util.List
            .of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
      }

      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          new JwtPayload(userId, role), null, authorities);

      authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authenticationToken);

      log.debug("시큐리티 컨텍스트에 인증 정보 저장 완료 - UserId: {}", userId);

    } catch (ExpiredJwtException e) {
      log.warn("만료된 액세스 토큰입니다. URI: {}", request.getRequestURI());
      throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
    } catch (Exception e) {
      log.error("JWT 인증 필터 오류. URI: {}", request.getRequestURI(), e);
      throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
    }

    filterChain.doFilter(request, response);
  }

  private String getAccessTokenFromHeader(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      return null;
    }

    return header.substring(7).trim();
  }
}
