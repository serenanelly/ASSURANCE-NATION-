package com.assurance.nation.service;

import com.assurance.nation.config.JwtTokenProvider;
import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.enums.UserType;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.mapper.UserMapper;
import com.assurance.nation.repository.RefreshTokenRepository;
import com.assurance.nation.repository.RoleRepository;
import com.assurance.nation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.jsonwebtoken.Claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;
    @Mock private MedecinService medecinService;
    @Mock private AssureService assureService;
    @Mock private AuditService auditService;
    @Mock private RoleAssignmentService roleAssignmentService;
    @InjectMocks private AuthService authService;

    @Test
    void login_returnsTokens() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("pass");
        Assureur user = new Assureur();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setRoles(Set.of(Role.builder().roleName(RoleName.ADMIN).build()));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn("refresh-value");
        when(jwtTokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(userMapper.toResponse(user)).thenReturn(new com.assurance.nation.dto.UserDTO.UserResponse());
        AuthDTO.AuthResponse response = authService.login(request);
        assertThat(response.getAccessToken()).isEqualTo("access");
    }

    @Test
    void validateToken_valid_returnsEmail() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.parseClaims("valid-token")).thenReturn(claims);
        when(claims.get("email", String.class)).thenReturn("user@test.com");

        AuthDTO.ValidateTokenResponse response = authService.validateToken("valid-token");

        assertThat(response.isValid()).isTrue();
        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getMessage()).isEqualTo("Token valide");
    }

    @Test
    void register_createsAssure() {
        AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("Pass1!");
        req.setConfirmPassword("Pass1!");
        req.setNom("Dupont");
        req.setPrenom("Jean");
        req.setUserType(UserType.ASSURE);
        req.setNumSecuriteSociale("123456789012345");
        Assure assure = new Assure();
        assure.setId(UUID.randomUUID());
        assure.setEmail("new@test.com");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(assureService.createFromRegister(req)).thenReturn(assure);
        when(userRepository.save(any())).thenReturn(assure);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshTokenValue()).thenReturn("refresh");
        when(jwtTokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
        when(userMapper.toResponse(any())).thenReturn(new com.assurance.nation.dto.UserDTO.UserResponse());
        AuthDTO.AuthResponse response = authService.register(req, "127.0.0.1");
        assertThat(response.getAccessToken()).isEqualTo("access");
    }

    @Test
    void refresh_returnsNewAccessToken() {
        com.assurance.nation.entity.RefreshToken stored = com.assurance.nation.entity.RefreshToken.builder()
                .token("rt")
                .revoked(false)
                .expiryDate(java.time.Instant.now().plusSeconds(3600))
                .user(new Assureur())
                .build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(stored));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("new-access");
        when(jwtTokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        AuthDTO.RefreshRequest req = new AuthDTO.RefreshRequest();
        req.setRefreshToken("rt");
        assertThat(authService.refresh(req).getAccessToken()).isEqualTo("new-access");
    }

    @Test
    void logout_revokesToken() {
        com.assurance.nation.entity.RefreshToken token = com.assurance.nation.entity.RefreshToken.builder()
                .token("rt").revoked(false).build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(java.util.Optional.of(token));
        authService.logout("rt");
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void validateToken_invalid_returnsFalse() {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        AuthDTO.ValidateTokenResponse response = authService.validateToken("bad-token");

        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("invalide");
    }
}
