package com.assurance.nation.controller;

import com.assurance.nation.dto.ReimbursementDTO;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import com.assurance.nation.security.RoleChecker;
import com.assurance.nation.service.ReimbursementService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * API de gestion des remboursements (workflow PENDING → APPROVED → PAID).
 */
@RestController
@RequestMapping(Constants.API_V1 + "/reimbursements")
@RequiredArgsConstructor
@Tag(name = "Remboursements", description = "Gestion des remboursements de soins")
@SecurityRequirement(name = "bearerAuth")
public class ReimbursementController {

    private final ReimbursementService reimbursementService;
    private final RoleChecker roleChecker;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Créer une demande de remboursement")
    @ApiResponse(responseCode = "201", description = "Remboursement créé (PENDING)")
    public ResponseEntity<ReimbursementDTO.Response> create(
            @Valid @RequestBody ReimbursementDTO.CreateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reimbursementService.create(request, httpRequest.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Lister les remboursements (ASSUREUR: tout, PATIENT: les siens)")
    public ResponseEntity<ReimbursementDTO.PageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReimbursementStatus status,
            @RequestParam(required = false) UUID assureId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        if (roleChecker.isPatient()) {
            return ResponseEntity.ok(reimbursementService.findForCurrentPatient(page, size));
        }
        return ResponseEntity.ok(reimbursementService.findAll(page, size, status, assureId, startDate, endDate));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Statistiques tableau de bord remboursements")
    public ResponseEntity<ReimbursementDTO.DashboardResponse> dashboard() {
        return ResponseEntity.ok(reimbursementService.dashboard());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Détails d'un remboursement")
    public ResponseEntity<ReimbursementDTO.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(reimbursementService.findById(id));
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Approuver un remboursement en attente")
    public ResponseEntity<ReimbursementDTO.Response> approve(
            @PathVariable UUID id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(reimbursementService.approve(id, httpRequest.getRemoteAddr()));
    }

    @RequestMapping(value = "/{id}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Rejeter un remboursement en attente")
    public ResponseEntity<ReimbursementDTO.Response> reject(
            @PathVariable UUID id,
            @Valid @RequestBody ReimbursementDTO.RejectRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(reimbursementService.reject(id, request, httpRequest.getRemoteAddr()));
    }

    @RequestMapping(value = "/{id}/mark-paid", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Marquer un remboursement approuvé comme payé")
    public ResponseEntity<ReimbursementDTO.Response> markPaid(
            @PathVariable UUID id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(reimbursementService.markPaid(id, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/{id}/justificatif")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Télécharger le justificatif PDF")
    @ApiResponse(responseCode = "200", description = "Fichier PDF")
    public ResponseEntity<Resource> justificatif(@PathVariable UUID id) throws IOException {
        Resource resource = reimbursementService.downloadJustificatif(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=justificatif.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
