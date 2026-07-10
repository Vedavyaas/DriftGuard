package com.pheonix.authentication.repository;

import com.pheonix.authentication.assets.Role;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    private boolean isEnabled;
    private Instant createdAt;
    private Instant lastUpdatedAt;

    public UserEntity() {
    }

    public UserEntity(String username, String email, String password, Role role, Instant lastUpdatedAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isEnabled = true;
        this.createdAt = Instant.now();
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
