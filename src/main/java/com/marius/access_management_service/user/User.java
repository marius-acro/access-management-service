package com.marius.access_management_service.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.marius.access_management_service.user.role.Role;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@JsonPropertyOrder({"id", "email", "role"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    private Role role;

    protected User() {
    }

    public User(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
