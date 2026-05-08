package org.all.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.all.auth.config.KeycloakProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    public KeycloakClient(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.webClient = WebClient.builder()
                .build();
    }

    public Mono<KeycloakTokenResponse> getToken(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", keycloakProperties.getClientId());
        
        String clientSecret = keycloakProperties.getClientSecret();
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }
        
        formData.add("username", username);
        formData.add("password", password);

        return webClient.post()
                .uri(keycloakProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class);
    }

    public Mono<KeycloakTokenResponse> refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.getClientId());
        
        String clientSecret = keycloakProperties.getClientSecret();
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }
        
        formData.add("refresh_token", refreshToken);

        return webClient.post()
                .uri(keycloakProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class);
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