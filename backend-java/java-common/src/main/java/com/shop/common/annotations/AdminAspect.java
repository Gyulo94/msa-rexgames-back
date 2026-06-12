package com.shop.common.annotations;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.shop.common.error.ErrorCode;
import com.shop.common.exception.ApiException;
import com.shop.common.jwt.JwtPayload;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AdminAspect {

    @Before("@annotation(com.shop.common.annotations.Admin) || @within(com.shop.common.annotations.Admin)")
    public void validateAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        boolean isAdmin = false;

        if (principal instanceof JwtPayload jwtPayload) {
            isAdmin = "ADMIN".equalsIgnoreCase(jwtPayload.getRole());
        } else {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equalsIgnoreCase(authority.getAuthority()));
        }

        if (!isAdmin) {
            log.warn("유저는 어드민이 아닙니다. : {}", principal);
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }
}
