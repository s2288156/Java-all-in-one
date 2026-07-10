package org.all.auth.service.impl;

import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.KeycloakTokenResponse;
import org.all.auth.client.KeycloakClient.RealmUser;
import org.all.auth.dto.*;
import org.all.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final KeycloakClient keycloakClient;
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public AuthServiceImpl(KeycloakClient keycloakClient,
                           @Value("${user-service.url}") String userServiceUrl) {
        this.keycloakClient = keycloakClient;
        this.restTemplate = new RestTemplate();
        this.userServiceUrl = userServiceUrl;
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
        String keycloakId = keycloakClient.createUser(user);

        syncUserToUserService(keycloakId, request.getEmail(), request.getUsername());

        KeycloakTokenResponse tokenResponse = keycloakClient.getToken(request.getEmail(), request.getPassword());
        return LoginResponse.builder()
                .token(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .tokenType(tokenResponse.getToken_type())
                .expiresIn(tokenResponse.getExpires_in())
                .build();
    }

    private void syncUserToUserService(String keycloakId, String email, String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    Map.of("keycloakId", keycloakId, "email", email, "username", username), headers);
            restTemplate.postForEntity(userServiceUrl + "/api/users/internal", request, Void.class);
        } catch (Exception e) {
            log.warn("Failed to sync user to user-service: {}", e.getMessage());
        }
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
