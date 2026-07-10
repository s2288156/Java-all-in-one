package org.all.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.all.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RoleAuthorizationInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            return true;
        }

        String rolesHeader = request.getHeader("X-User-Roles");
        List<String> userRoles = (rolesHeader != null && !rolesHeader.isBlank())
                ? Arrays.asList(rolesHeader.split(","))
                : Collections.emptyList();

        String[] requiredRoles = annotation.value();
        boolean hasAny = Arrays.stream(requiredRoles).anyMatch(userRoles::contains);

        if (!hasAny) {
            log.warn("Access denied: user roles {} do not satisfy required roles {}",
                    userRoles, Arrays.asList(requiredRoles));
            throw new BusinessException(403, "权限不足");
        }

        return true;
    }
}
