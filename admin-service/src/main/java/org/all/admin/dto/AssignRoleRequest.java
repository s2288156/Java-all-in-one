package org.all.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {

    @NotEmpty(message = "角色列表不能为空")
    private List<String> roleNames;
}
