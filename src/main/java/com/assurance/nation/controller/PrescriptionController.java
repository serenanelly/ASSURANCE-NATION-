package com.assurance.nation.controller;

import com.assurance.nation.dto.PrescriptionDTO;
import com.assurance.nation.service.PrescriptionService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Ordonnances et prescriptions de consultation")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping(Constants.API_V1 + "/consultations/{id}/prescriptions")
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Ajouter une prescription à une consultation")
    public ResponseEntity<PrescriptionDTO.Response> create(
            @PathVariable("id") UUID consultationId,
            @Valid @RequestBody PrescriptionDTO.CreateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(consultationId, request, httpRequest.getRemoteAddr()));
    }

    @GetMapping(Constants.API_V1 + "/consultations/{id}/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Lister les prescriptions d'une consultation")
    public ResponseEntity<List<PrescriptionDTO.Response>> listByConsultation(@PathVariable("id") UUID consultationId) {
        return ResponseEntity.ok(prescriptionService.findByConsultationId(consultationId));
    }

    @GetMapping(Constants.API_V1 + "/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Lister les prescriptions")
    public ResponseEntity<List<PrescriptionDTO.Response>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(prescriptionService.findAll(page, size));
    }

    @GetMapping(Constants.API_V1 + "/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','ASSUREUR','PATIENT')")
    @Operation(summary = "Détails d'une prescription")
    public ResponseEntity<PrescriptionDTO.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.findById(id));
    }

    @DeleteMapping(Constants.API_V1 + "/prescriptions/{id}")
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Supprimer logiquement une prescription")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestBody(required = false) PrescriptionDTO.DeleteRequest body,
            HttpServletRequest httpRequest) {
        String motif = body != null ? body.getMotifSuppression() : null;
        prescriptionService.delete(id, motif, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
