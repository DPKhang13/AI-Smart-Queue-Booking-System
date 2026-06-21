package com.personal.ai_sqbs.security;

import com.personal.ai_sqbs.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String username;
    private final String phone;
    private final String avatarUrl;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.phone = user.getPhone();
        this.avatarUrl = user.getAvatarUrl();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole().getName();
        this.enabled = Boolean.TRUE.equals(user.getIsActive())
                && !Boolean.TRUE.equals(user.getIsDeleted())
                && Boolean.TRUE.equals(user.getEmailVerified());
        this.authorities = List.of(new SimpleGrantedAuthority(toAuthorityName(role)));
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getLoginUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username != null ? username : email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private String toAuthorityName(String roleName) {
        if (roleName == null || roleName.startsWith("ROLE_")) {
            return roleName;
        }

        return "ROLE_" + roleName;
    }
}
