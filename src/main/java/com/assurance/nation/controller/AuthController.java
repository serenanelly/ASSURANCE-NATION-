package com.assurance.nation.controller;

import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.service.AuthService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API d'authentification JWT (inscription, connexion, refresh, validation).
 */
@RestController
@RequestMapping(Constants.API_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, refresh et déconnexion JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Enregistrer un utilisateur")
    @ApiResponse(responseCode = "201", description = "Inscription réussie")
    public ResponseEntity<AuthDTO.AuthResponse> register(
            @Valid @RequestBody AuthDTO.RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion")
    @ApiResponse(responseCode = "200", description = "JWT émis")
    public ResponseEntity<AuthDTO.AuthResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    public ResponseEntity<AuthDTO.TokenResponse> refresh(@Valid @RequestBody AuthDTO.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/validate")
    @Operation(summary = "Valider un token JWT")
    public ResponseEntity<AuthDTO.ValidateTokenResponse> validate(
            @Valid @RequestBody AuthDTO.ValidateTokenRequest request) {
        return ResponseEntity.ok(authService.validateToken(request.getToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion (révocation refresh token)")
    @ApiResponse(responseCode = "204", description = "Déconnecté")
    public ResponseEntity<Void> logout(@Valid @RequestBody AuthDTO.RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
