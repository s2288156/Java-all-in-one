package org.all.auth.service.impl;

import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.KeycloakTokenResponse;
import org.all.auth.client.KeycloakClient.RealmUser;
import org.all.auth.dto.*;
import org.all.auth.service.AuthService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final KeycloakClient keycloakClient;

    public AuthServiceImpl(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        KeycloakTokenResponse response = keycloakClient.getToken(request.getEmail(), request.getPassword());
        return LoginResponse.builder()
                .token(response.getAccess_token())
                .refreshToken(response.getRefresh_token())
                .tokenType(response.getToken_type())
                .expiresIn(response.getExpires_in())
                .build();
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        KeycloakClient.CredentialRepresentation credential = new KeycloakClient.CredentialRepresentation();
        credential.setType("password");
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        RealmUser user = RealmUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .enabled(true)
                .emailVerified(false)
                .credentials(List.of(credential))
                .build();
        keycloakClient.createUser(user);

        KeycloakTokenResponse tokenResponse = keycloakClient.getToken(request.getEmail(), request.getPassword());
        return LoginResponse.builder()
                .token(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .tokenType(tokenResponse.getToken_type())
                .expiresIn(tokenResponse.getExpires_in())
                .build();
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        KeycloakTokenResponse response = keycloakClient.refreshToken(refreshToken);
        return LoginResponse.builder()
                .token(response.getAccess_token())
                .refreshToken(response.getRefresh_token())
                .tokenType(response.getToken_type())
                .expiresIn(response.getExpires_in())
                .build();
    }

    @Override
    public void logout(String userId) {
        keycloakClient.logoutUser(userId);
    }
}
