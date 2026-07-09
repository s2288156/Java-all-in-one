package org.all.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.all.auth.config.KeycloakProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakClient {

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = new RestTemplate();
    }

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
}
