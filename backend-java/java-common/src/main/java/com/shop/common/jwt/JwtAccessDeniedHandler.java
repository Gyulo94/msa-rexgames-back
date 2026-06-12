package com.shop.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.api.Api;
import com.shop.common.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    log.warn("접근 거부 - PATH: {}", request.getRequestURI());

    response.setStatus(ErrorCode.FORBIDDEN.getHttpStatusCode());
    response.setContentType("application/json;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    Api<Object> errorResponse = Api.ERROR(ErrorCode.FORBIDDEN);

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
