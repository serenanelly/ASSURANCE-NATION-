package com.assurance.nation.controller;

import com.assurance.nation.dto.ConsultationDTO;
import com.assurance.nation.service.ConsultationService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API des consultations médicales (création, modification, annulation).
 */
@RestController
@RequestMapping(Constants.API_V1 + "/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultations", description = "Consultations médicales")
@SecurityRequirement(name = "bearerAuth")
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Créer une consultation (MEDECIN)")
    @ApiResponse(responseCode = "201", description = "Consultation et feuille de maladie créées")
    public ResponseEntity<ConsultationDTO.Response> create(
            @Valid @RequestBody ConsultationDTO.CreateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.create(request, httpRequest.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Lister les consultations (filtré par rôle)")
    public ResponseEntity<List<ConsultationDTO.Response>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultationService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Détails d'une consultation")
    public ResponseEntity<ConsultationDTO.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(consultationService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Modifier / reporter une consultation")
    public ResponseEntity<ConsultationDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ConsultationDTO.UpdateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(consultationService.update(id, request, httpRequest.getRemoteAddr()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Annuler une consultation (soft delete)")
    @ApiResponse(responseCode = "204", description = "Consultation annulée")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) ConsultationDTO.CancelRequest request,
            HttpServletRequest httpRequest) {
        consultationService.cancel(id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
