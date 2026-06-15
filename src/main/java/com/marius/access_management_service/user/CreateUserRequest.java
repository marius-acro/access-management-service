package com.marius.access_management_service.user;

import com.marius.access_management_service.user.role.Role;
import com.marius.access_management_service.user.role.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotNull @ValidRole String role
) {
    Role toRole() {
        return Role.valueOf(this.role);
    }
}
