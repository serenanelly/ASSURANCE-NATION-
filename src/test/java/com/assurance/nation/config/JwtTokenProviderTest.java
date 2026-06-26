package com.assurance.nation.config;

import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.enums.RoleName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void generateAndValidateToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret("TestSecretKeyForJWTSigningMustBeAtLeast256BitsLong2026Testing!");
        props.setAccessExpirationMs(900000);
        JwtTokenProvider provider = new JwtTokenProvider(props);
        Assureur user = new Assureur();
        user.setId(UUID.randomUUID());
        user.setEmail("u@test.com");
        user.setRoles(Set.of(Role.builder().roleName(RoleName.ADMIN).build()));
        String token = provider.generateAccessToken(user);
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserIdFromToken(token)).isEqualTo(user.getId());
    }
}
