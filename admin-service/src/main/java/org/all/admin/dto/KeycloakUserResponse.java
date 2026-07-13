package org.all.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserResponse {

    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private String createdAt;
}
