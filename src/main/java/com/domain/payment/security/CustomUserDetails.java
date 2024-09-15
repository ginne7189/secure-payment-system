package com.domain.payment.security;

import com.domain.payment.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final Set<GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public CustomUserDetails(User user, Set<GrantedAuthority> authorities) {
        this(user.getUsername(), user.getPassword(), authorities, true, true, true, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}