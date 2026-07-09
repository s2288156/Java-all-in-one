package org.all.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
