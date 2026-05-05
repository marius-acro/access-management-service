package com.marius.access_management_service.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String email,
        @NotNull Role role
) {
}
