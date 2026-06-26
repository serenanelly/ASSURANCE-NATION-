package com.assurance.nation.service;

import com.assurance.nation.config.JwtTokenProvider;
import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.entity.*;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.entity.enums.UserType;
import com.assurance.nation.exception.DuplicateResourceException;
import com.assurance.nation.util.ValidationUtil;
import com.assurance.nation.exception.UnauthorizedException;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.mapper.UserMapper;
import com.assurance.nation.repository.RefreshTokenRepository;
import com.assurance.nation.repository.RoleRepository;
import com.assurance.nation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final MedecinService medecinService;
    private final AssureService assureService;
    private final AuditService auditService;
    private final RoleAssignmentService roleAssignmentService;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request, String ip) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email déjà utilisé");
        }
        ValidationUtil.validatePasswordMatch(request.getPassword(), request.getConfirmPassword());
        User user = switch (request.getUserType()) {
            case MEDECIN -> medecinService.createFromRegister(request);
            case ASSURE -> assureService.createFromRegister(request);
            case ASSUREUR -> createAssureur(request);
        };
        user = userRepository.save(user);
        auditService.log("User", user.getId().toString(), com.assurance.nation.entity.enums.AuditAction.CREATE,
                null, null, userMapper.toResponse(user), ip);
        return buildAuthResponse(user, "Inscription réussie");
    }

    private Assureur createAssureur(AuthDTO.RegisterRequest request) {
        Assureur assureur = new Assureur();
        fillUser(assureur, request);
        roleAssignmentService.assignRole(assureur, RoleName.ASSUREUR);
        return assureur;
    }

    private void fillUser(User user, AuthDTO.RegisterRequest request) {
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setDateNaissance(request.getDateNaissance());
        user.setLieuNaissance(request.getLieuNaissance());
        user.setAdresse(request.getAdresse());
        user.setTelephone(request.getTelephone());
        user.setSexe(request.getSexe());
        user.setUserType(request.getUserType());
        user.setPhotoUrl(request.getPhotoUrl());
    }

    @Transactional
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            throw new UnauthorizedException("Identifiants invalides");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
        return buildAuthResponse(user, "Connexion réussie");
    }

    @Transactional
    public AuthDTO.TokenResponse refresh(AuthDTO.RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide"));
        if (stored.isRevoked() || stored.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expiré ou révoqué");
        }
        User user = stored.getUser();
        String access = jwtTokenProvider.generateAccessToken(user);
        AuthDTO.TokenResponse response = new AuthDTO.TokenResponse();
        response.setAccessToken(access);
        response.setRefreshToken(stored.getToken());
        response.setExpiresIn(jwtTokenProvider.getAccessExpirationMs() / 1000);
        return response;
    }

    @Transactional(readOnly = true)
    public AuthDTO.ValidateTokenResponse validateToken(String token) {
        AuthDTO.ValidateTokenResponse response = new AuthDTO.ValidateTokenResponse();
        if (!jwtTokenProvider.validateToken(token)) {
            response.setValid(false);
            response.setMessage("Token invalide ou expiré");
            return response;
        }
        try {
            String email = jwtTokenProvider.parseClaims(token).get("email", String.class);
            response.setValid(true);
            response.setEmail(email);
            response.setMessage("Token valide");
        } catch (Exception e) {
            response.setValid(false);
            response.setMessage("Token invalide");
        }
        return response;
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthDTO.AuthResponse buildAuthResponse(User user, String message) {
        refreshTokenRepository.deleteByUserId(user.getId());
        String access = jwtTokenProvider.generateAccessToken(user);
        String refreshValue = jwtTokenProvider.generateRefreshTokenValue();
        RefreshToken refresh = RefreshToken.builder()
                .token(refreshValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refresh);
        AuthDTO.AuthResponse response = new AuthDTO.AuthResponse();
        response.setAccessToken(access);
        response.setRefreshToken(refreshValue);
        response.setExpiresIn(jwtTokenProvider.getAccessExpirationMs() / 1000);
        response.setUser(userMapper.toResponse(user));
        response.setMessage(message);
        return response;
    }
}
