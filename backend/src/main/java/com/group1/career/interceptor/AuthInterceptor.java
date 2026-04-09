package com.group1.career.interceptor;

import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        // Assuming Bearer token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !JwtUtils.validateToken(token)) {
            throw new BizException(ErrorCode.UNAUTHORIZED_ERROR, "Invalid or missing token");
        }

        // Parse user id and store in request attributes for controllers to use
        Long userId = JwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }
}
