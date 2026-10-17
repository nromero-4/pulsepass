package com.pulsepass.pulsepass.security;

import com.pulsepass.pulsepass.domain.User;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final User user;
    private final String passwordHash;

    private UserPrincipal(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user, null);
    }

    public static UserPrincipal create(User user, String passwordHash) {
        return new UserPrincipal(user, passwordHash);
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(user.getActive());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getActive());
    }
}
