package org.all.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private String userId;
    private String email;
    private List<String> roles;

    public static UserContext fromHeaders(HttpHeaders headers) {
        String userId = headers.getFirst("X-User-Id");
        String email = headers.getFirst("X-User-Email");
        String rolesStr = headers.getFirst("X-User-Roles");

        List<String> roles = (rolesStr != null && !rolesStr.isBlank())
                ? Arrays.asList(rolesStr.split(","))
                : Collections.emptyList();

        return UserContext.builder()
                .userId(userId)
                .email(email)
                .roles(roles)
                .build();
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
