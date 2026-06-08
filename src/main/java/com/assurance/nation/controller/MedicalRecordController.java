package com.assurance.nation.controller;

import com.assurance.nation.dto.MedicalRecordDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.service.MedicalRecordService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * API des feuilles de maladie (liste par assuré, détail, mise à jour).
 */
@RestController
@RequestMapping(Constants.API_V1 + "/medical-records")
@RequiredArgsConstructor
@Tag(name = "Feuilles de maladie", description = "Dossiers médicaux / feuilles de maladie")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/{assureId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Lister les feuilles de maladie d'un assuré")
    public ResponseEntity<PageDTO<MedicalRecordDTO.Response>> listByAssure(
            @PathVariable UUID assureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(medicalRecordService.findByAssureId(assureId, page, size, startDate, endDate, status));
    }

    @GetMapping(params = "assureId")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Lister les feuilles de maladie (alias param assureId)")
    public ResponseEntity<PageDTO<MedicalRecordDTO.Response>> listByAssureParam(
            @RequestParam UUID assureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(medicalRecordService.findByAssureId(assureId, page, size, startDate, endDate, status));
    }

    @GetMapping("/record/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Détails d'une feuille de maladie")
    public ResponseEntity<MedicalRecordDTO.Response> get(@PathVariable UUID id) {
        return ResponseEntity.ok(medicalRecordService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN')")
    @Operation(summary = "Modifier une feuille de maladie (si non remboursée)")
    @ApiResponse(responseCode = "200", description = "Feuille mise à jour")
    public ResponseEntity<MedicalRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody MedicalRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(medicalRecordService.update(id, request));
    }
}
