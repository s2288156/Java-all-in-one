package org.all.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.all.auth.config.KeycloakProperties;
import org.all.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakClient.class);

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;
    private final ObjectMapper objectMapper;

    private String adminAccessToken;
    private Instant adminTokenExpiresAt = Instant.MIN;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    keycloakProperties.getTokenUri(), request, KeycloakTokenResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            String detail = parseKeycloakError(e);
            throw new BusinessException(e.getStatusCode().value(),
                    "获取 token 失败: " + detail);
        } catch (ResourceAccessException e) {
            throw new BusinessException(500, "无法连接到 Keycloak: " + e.getMessage());
        }
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

        String tokenUri = keycloakProperties.getTokenUri();
        log.info("Requesting admin token from: {}", tokenUri);

        // Try client_credentials first
        try {
            return requestClientCredentialsToken(tokenUri);
        } catch (HttpClientErrorException e) {
            log.warn("client_credentials grant failed (status={}): {}. Trying password grant.",
                    e.getStatusCode(), parseKeycloakError(e));
        }

        // Fallback to password grant
        String adminUsername = keycloakProperties.getAdminUsername();
        String adminPassword = keycloakProperties.getAdminPassword();
        if (adminUsername != null && !adminUsername.isEmpty()
                && adminPassword != null && !adminPassword.isEmpty()) {
            try {
                return requestPasswordToken(tokenUri, adminUsername, adminPassword);
            } catch (HttpClientErrorException e) {
                String detail = parseKeycloakError(e);
                throw new BusinessException(500, "获取 Keycloak 管理员 token 失败: " + detail);
            }
        }

        throw new BusinessException(500, "获取 Keycloak 管理员 token 失败: client_credentials 被拒绝，且未配置 admin 用户名/密码");
    }

    private String requestClientCredentialsToken(String tokenUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakProperties.getAdminClientId());

        String adminSecret = keycloakProperties.getAdminClientSecret();
        if (adminSecret != null && !adminSecret.isEmpty()) {
            formData.add("client_secret", adminSecret);
        }

        return postToken(tokenUri, formData);
    }

    private String requestPasswordToken(String tokenUri, String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakProperties.getAdminClientId());
        formData.add("username", username);
        formData.add("password", password);

        String adminSecret = keycloakProperties.getAdminClientSecret();
        if (adminSecret != null && !adminSecret.isEmpty()) {
            formData.add("client_secret", adminSecret);
        }

        return postToken(tokenUri, formData);
    }

    private String postToken(String tokenUri, MultiValueMap<String, String> formData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    tokenUri, request, KeycloakTokenResponse.class);

            KeycloakTokenResponse body = response.getBody();
            adminAccessToken = body.getAccess_token();
            adminTokenExpiresAt = Instant.now().plusSeconds(body.getExpires_in());
            return adminAccessToken;
        } catch (HttpClientErrorException e) {
            throw e; // let caller handle
        } catch (HttpServerErrorException e) {
            throw new BusinessException(500, "Keycloak 服务器错误: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new BusinessException(500, "无法连接到 Keycloak: " + e.getMessage());
        }
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
        try {
            restTemplate.postForEntity(adminBaseUrl() + "/users", request, Void.class);
        } catch (HttpClientErrorException e) {
            String detail = parseKeycloakError(e);
            throw new BusinessException(e.getStatusCode().value(),
                    "创建用户失败: " + detail);
        } catch (ResourceAccessException e) {
            throw new BusinessException(500, "无法连接到 Keycloak: " + e.getMessage());
        }
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
        try {
            ResponseEntity<List<RealmUser>> response = restTemplate.exchange(
                    adminBaseUrl() + "/users?first=" + first + "&max=" + max,
                    HttpMethod.GET, request,
                    new org.springframework.core.ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (HttpClientErrorException e) {
            String detail = parseKeycloakError(e);
            throw new BusinessException(500, "获取用户列表失败: " + detail);
        } catch (ResourceAccessException e) {
            throw new BusinessException(500, "无法连接到 Keycloak: " + e.getMessage());
        }
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

    // ========== Error Handling ==========

    private String parseKeycloakError(HttpClientErrorException e) {
        try {
            JsonNode json = objectMapper.readTree(e.getResponseBodyAsString());
            if (json.has("error_description")) {
                return json.get("error_description").asText();
            }
            if (json.has("errorMessage")) {
                return json.get("errorMessage").asText();
            }
            return e.getResponseBodyAsString();
        } catch (Exception ignored) {
            return e.getStatusText();
        }
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
        private List<CredentialRepresentation> credentials;
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
