package org.all.auth.service;

import org.all.auth.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    LoginResponse refresh(String refreshToken);
    void logout(String userId);
}
