package org.all.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String tokenUri;
    private String adminClientId = "admin-cli";
    private String adminClientSecret;
    private String adminUsername;
    private String adminPassword;
}
