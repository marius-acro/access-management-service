package com.marius.access_management_service.user.role;

public enum Role {
    ADMIN,
    MEMBER,
    GUEST;

    public boolean canManageUsers() {
        return this == ADMIN;
    }
}

