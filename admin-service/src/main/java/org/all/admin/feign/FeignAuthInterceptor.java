package org.all.admin.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final List<String> HEADER_NAMES = List.of(
            "X-User-Id", "X-Username", "X-User-Email", "X-User-Roles", "Authorization"
    );

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }

        HttpServletRequest request = attrs.getRequest();
        for (String headerName : HEADER_NAMES) {
            String value = request.getHeader(headerName);
            if (value != null) {
                template.header(headerName, value);
            }
        }
    }
}
