package org.all.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String keycloakId;
    private String email;
    private String username;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
