package org.all.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.all.auth.config.KeycloakProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakClient {

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;

    private String adminAccessToken;
    private Instant adminTokenExpiresAt = Instant.MIN;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
    }

    // ========== OIDC Token Endpoints ==========

    public KeycloakTokenResponse getToken(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakProperties.getClientId());

        String clientSecret = keycloakProperties.getClientSecret();
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }

        formData.add("username", username);
        formData.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                keycloakProperties.getTokenUri(), request, KeycloakTokenResponse.class);

        return response.getBody();
    }

    public KeycloakTokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.getClientId());

        String clientSecret = keycloakProperties.getClientSecret();
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }

        formData.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                keycloakProperties.getTokenUri(), request, KeycloakTokenResponse.class);

        return response.getBody();
    }

    // ========== Admin Token ==========

    private synchronized String getAdminAccessToken() {
        if (adminAccessToken != null && Instant.now().isBefore(adminTokenExpiresAt.minusSeconds(30))) {
            return adminAccessToken;
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakProperties.getAdminClientId());

        String adminSecret = keycloakProperties.getAdminClientSecret();
        if (adminSecret != null && !adminSecret.isEmpty()) {
            formData.add("client_secret", adminSecret);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                keycloakProperties.getTokenUri(), request, KeycloakTokenResponse.class);

        KeycloakTokenResponse body = response.getBody();
        adminAccessToken = body.getAccess_token();
        adminTokenExpiresAt = Instant.now().plusSeconds(body.getExpires_in());
        return adminAccessToken;
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminAccessToken());
        return headers;
    }

    private String adminBaseUrl() {
        return keycloakProperties.getServerUrl() + "/admin/realms/" + keycloakProperties.getRealm();
    }

    // ========== User Management (Admin API) ==========

    public void createUser(RealmUser user) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<RealmUser> request = new HttpEntity<>(user, headers);
        restTemplate.postForEntity(adminBaseUrl() + "/users", request, Void.class);
    }

    public RealmUser getUser(String userId) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<RealmUser> response = restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId, HttpMethod.GET, request, RealmUser.class);
        return response.getBody();
    }

    public List<RealmUser> getUserByEmail(String email) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List<RealmUser>> response = restTemplate.exchange(
                adminBaseUrl() + "/users?email=" + email, HttpMethod.GET, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public List<RealmUser> getAllUsers(int first, int max) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List<RealmUser>> response = restTemplate.exchange(
                adminBaseUrl() + "/users?first=" + first + "&max=" + max,
                HttpMethod.GET, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public void updateUser(String userId, Map<String, Object> fields) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(fields, headers);
        restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId, HttpMethod.PUT, request, Void.class);
    }

    public void deleteUser(String userId) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId, HttpMethod.DELETE, request, Void.class);
    }

    public void resetPassword(String userId, String newPassword, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType("password");
        credential.setValue(newPassword);
        credential.setTemporary(temporary);

        HttpHeaders headers = adminHeaders();
        HttpEntity<CredentialRepresentation> request = new HttpEntity<>(credential, headers);
        restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId + "/reset-password",
                HttpMethod.PUT, request, Void.class);
    }

    // ========== Role Management (Admin API) ==========

    public List<RoleRepresentation> getRoles() {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List<RoleRepresentation>> response = restTemplate.exchange(
                adminBaseUrl() + "/roles", HttpMethod.GET, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public void createRole(RoleRepresentation role) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<RoleRepresentation> request = new HttpEntity<>(role, headers);
        restTemplate.postForEntity(adminBaseUrl() + "/roles", request, Void.class);
    }

    public void deleteRole(String roleName) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                adminBaseUrl() + "/roles/" + roleName, HttpMethod.DELETE, request, Void.class);
    }

    public List<RoleRepresentation> getUserRoles(String userId) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<List<RoleRepresentation>> response = restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId + "/role-mappings/realm",
                HttpMethod.GET, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public void assignUserRoles(String userId, List<RoleRepresentation> roles) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<List<RoleRepresentation>> request = new HttpEntity<>(roles, headers);
        restTemplate.postForEntity(
                adminBaseUrl() + "/users/" + userId + "/role-mappings/realm",
                request, Void.class);
    }

    public void removeUserRoles(String userId, List<RoleRepresentation> roles) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<List<RoleRepresentation>> request = new HttpEntity<>(roles, headers);
        restTemplate.exchange(
                adminBaseUrl() + "/users/" + userId + "/role-mappings/realm",
                HttpMethod.DELETE, request, Void.class);
    }

    // ========== Logout ==========

    public void logoutUser(String userId) {
        HttpHeaders headers = adminHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.postForEntity(
                adminBaseUrl() + "/users/" + userId + "/logout", request, Void.class);
    }

    // ========== Inner DTOs ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeycloakTokenResponse {
        private String access_token;
        private String token_type;
        private long expires_in;
        private String refresh_token;
        private String scope;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RealmUser {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean enabled;
        private Boolean emailVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleRepresentation {
        private String id;
        private String name;
        private String description;
        private Boolean composite;
        private Boolean clientRole;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialRepresentation {
        private String type;
        private String value;
        private Boolean temporary;
    }
}
