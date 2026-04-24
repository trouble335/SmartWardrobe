package com.smartwardrobe.interceptor;

import com.smartwardrobe.config.JwtConfig;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.mapper.UserMapper;
import com.smartwardrobe.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.debug("请求拦截: {}", requestURI);

        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(30001, "未登录");
        }

        if (!jwtConfig.validateToken(token)) {
            throw new BusinessException(30002, "登录过期");
        }

        Long userId = jwtConfig.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(30002, "登录过期");
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(30003, "用户已被禁用");
        }

        request.setAttribute("userId", userId);
        request.setAttribute("user", user);

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
