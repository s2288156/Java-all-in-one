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
        KeycloakTokenResponse response = keycloakClient.getToken(request.getUsername(), request.getPassword());
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

        Map<String, List<String>> attributes = new java.util.HashMap<>();
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            attributes.put("phone", List.of(request.getPhone()));
        }

        RealmUser user = RealmUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .enabled(true)
                .emailVerified(false)
                .credentials(List.of(credential))
                .attributes(attributes.isEmpty() ? null : attributes)
                .build();
        String keycloakId = keycloakClient.createUser(user);

        syncUserToUserService(keycloakId, request.getEmail(), request.getUsername(), request.getPhone());

        KeycloakTokenResponse tokenResponse = keycloakClient.getToken(request.getUsername(), request.getPassword());
        return LoginResponse.builder()
                .token(tokenResponse.getAccess_token())
                .refreshToken(tokenResponse.getRefresh_token())
                .tokenType(tokenResponse.getToken_type())
                .expiresIn(tokenResponse.getExpires_in())
                .build();
    }

    private void syncUserToUserService(String keycloakId, String email, String username, String phone) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = new java.util.HashMap<>();
            body.put("keycloakId", keycloakId);
            body.put("username", username);
            if (email != null) {
                body.put("email", email);
            }
            if (phone != null) {
                body.put("phone", phone);
            }
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
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
