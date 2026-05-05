package com.marius.access_management_service.user;

public enum Role {
    ADMIN,
    MEMBER,
    GUEST;

    public boolean canManageUsers() {
        return this == ADMIN;
    }
}

