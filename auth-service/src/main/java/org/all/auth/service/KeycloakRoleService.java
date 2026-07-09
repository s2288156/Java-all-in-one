package org.all.auth.service;

import org.all.auth.client.KeycloakClient.RoleRepresentation;
import org.all.auth.dto.RoleResponse;

import java.util.List;

public interface KeycloakRoleService {
    List<RoleResponse> getRoles();
    void createRole(String name, String description);
    void deleteRole(String roleName);
    List<RoleResponse> getUserRoles(String userId);
    void assignRoles(String userId, List<String> roleNames);
    void removeRoles(String userId, List<String> roleNames);
}
