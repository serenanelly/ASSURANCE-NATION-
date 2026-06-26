package com.assurance.nation.controller;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.AuditLog;
import com.assurance.nation.service.AuditService;
import com.assurance.nation.service.UserService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * API de gestion des comptes utilisateurs (ADMIN ou propriétaire).
 */
@RestController
@RequestMapping(Constants.API_V1 + "/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les utilisateurs (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Liste paginée")
    public ResponseEntity<UserDTO.PageResponse<UserDTO.UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.canAccessUser(#id)")
    @Operation(summary = "Détails d'un utilisateur (soi-même ou ADMIN)")
    public ResponseEntity<UserDTO.UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.canModifyUser(#id)")
    @Operation(summary = "Modifier un utilisateur (soi-même ou ADMIN)")
    public ResponseEntity<UserDTO.UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.update(id, request, httpRequest.getRemoteAddr()));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.canModifyUser(#id)")
    @Operation(summary = "Changer le mot de passe (soi-même)")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipService.canDeleteUser(#id)")
    @Operation(summary = "Suppression logique (ADMIN ou soi-même)")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, HttpServletRequest httpRequest) {
        userService.softDelete(id, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Journal d'audit (ADMIN)")
    public ResponseEntity<UserDTO.PageResponse<AuditLog>> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.toPage(auditService.findAll(PageRequest.of(page, size))));
    }
}
