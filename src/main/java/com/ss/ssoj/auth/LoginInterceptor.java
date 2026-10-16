package com.ss.ssoj.auth;

import com.ss.ssoj.common.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：
 * 1. 接口标注 @Public 时允许游客访问，若携带了有效令牌则同样注入当前用户；
 * 2. 其余接口必须携带有效令牌，否则返回 401；
 * 3. 标注 @RequireAdmin 的接口仅管理员可访问，否则返回 403。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean isPublic = handlerMethod.hasMethodAnnotation(Public.class)
                || handlerMethod.getBeanType().isAnnotationPresent(Public.class);
        boolean requireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);

        String token = resolveToken(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.parse(token);
                UserContext.set(new UserContext.CurrentUser(
                        Long.valueOf(claims.getSubject()),
                        claims.get("username", String.class),
                        claims.get("role", Integer.class)));
            } catch (Exception ex) {
                // 公开接口携带无效令牌时按游客处理，受保护接口直接拒绝
                if (!isPublic) {
                    throw new BusinessException(401, "登录状态已过期，请重新登录");
                }
            }
        } else if (!isPublic) {
            throw new BusinessException(401, "登录状态已过期，请重新登录");
        }

        if (requireAdmin && !UserContext.isAdmin()) {
            throw new BusinessException(403, "没有权限执行该操作");
        }
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank() || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
