package com.assurance.nation.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleCheckerTest {

    private final RoleChecker roleChecker = new RoleChecker();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAdmin_true() {
        setRoles("ROLE_ADMIN");
        assertThat(roleChecker.isAdmin()).isTrue();
    }

    @Test
    void isAssureur_falseWhenPatient() {
        setRoles("ROLE_PATIENT");
        assertThat(roleChecker.isAssureur()).isFalse();
    }

    private void setRoles(String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", "x", authorities));
    }
}
