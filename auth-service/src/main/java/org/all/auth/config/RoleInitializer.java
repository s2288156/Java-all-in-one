package org.all.auth.config;

import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);

    private static final List<String> DEFAULT_ROLES = List.of("ROLE_USER", "ROLE_ADMIN");

    private final KeycloakClient keycloakClient;

    public RoleInitializer(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public void run(String... args) {
        try {
            List<String> existingRoles = keycloakClient.getRoles().stream()
                    .map(RoleRepresentation::getName)
                    .toList();

            for (String roleName : DEFAULT_ROLES) {
                if (!existingRoles.contains(roleName)) {
                    RoleRepresentation role = RoleRepresentation.builder()
                            .name(roleName)
                            .description(roleName.equals("ROLE_USER") ? "普通用户" : "管理员")
                            .build();
                    keycloakClient.createRole(role);
                    log.info("Created role: {}", roleName);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize default roles: {}", e.getMessage());
        }
    }
}
