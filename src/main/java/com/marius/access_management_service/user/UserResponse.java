package com.marius.access_management_service.user;

import com.marius.access_management_service.user.role.Role;

import java.util.UUID;

public record UserResponse(UUID id, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
