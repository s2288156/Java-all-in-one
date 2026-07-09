package org.all.auth.service.impl;

import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.RoleRepresentation;
import org.all.auth.dto.RoleResponse;
import org.all.auth.service.KeycloakRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakRoleServiceImpl implements KeycloakRoleService {

    private final KeycloakClient keycloakClient;

    public KeycloakRoleServiceImpl(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public List<RoleResponse> getRoles() {
        List<RoleRepresentation> roles = keycloakClient.getRoles();
        return roles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void createRole(String name, String description) {
        RoleRepresentation role = RoleRepresentation.builder()
                .name(name)
                .description(description)
                .build();
        keycloakClient.createRole(role);
    }

    @Override
    public void deleteRole(String roleName) {
        keycloakClient.deleteRole(roleName);
    }

    @Override
    public List<RoleResponse> getUserRoles(String userId) {
        List<RoleRepresentation> roles = keycloakClient.getUserRoles(userId);
        return roles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void assignRoles(String userId, List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> RoleRepresentation.builder().name(name).build())
                .collect(Collectors.toList());
        keycloakClient.assignUserRoles(userId, roles);
    }

    @Override
    public void removeRoles(String userId, List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
                .map(name -> RoleRepresentation.builder().name(name).build())
                .collect(Collectors.toList());
        keycloakClient.removeUserRoles(userId, roles);
    }

    private RoleResponse toResponse(RoleRepresentation role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
