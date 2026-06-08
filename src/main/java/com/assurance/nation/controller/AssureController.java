package com.assurance.nation.controller;

import com.assurance.nation.dto.AssureDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.service.AssureService;
import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Constants.API_V1 + "/users/assures")
@RequiredArgsConstructor
@Tag(name = "Assurés", description = "Gestion des assurés / patients")
@SecurityRequirement(name = "bearerAuth")
public class AssureController {

    private final AssureService assureService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Enregistrer un assuré (ASSUREUR)")
    public ResponseEntity<AssureDTO.AssureResponse> register(@Valid @RequestBody AssureDTO.RegisterAssureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assureService.register(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN')")
    @Operation(summary = "Lister les assurés (paginé, recherche)")
    public ResponseEntity<PageDTO<AssureDTO.AssureResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean actif) {
        return ResponseEntity.ok(assureService.findAll(page, size, search, actif));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN')")
    @Operation(summary = "Rechercher un assuré par numéro de sécurité sociale")
    public ResponseEntity<AssureDTO.AssureResponse> searchByNss(@RequestParam String nss) {
        return ResponseEntity.ok(assureService.searchByNSS(nss));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Détails d'un assuré")
    public ResponseEntity<AssureDTO.AssureResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(assureService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Modifier un assuré")
    public ResponseEntity<AssureDTO.AssureResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AssureDTO.UpdateAssureRequest request) {
        return ResponseEntity.ok(assureService.update(id, request));
    }

    @PutMapping("/{id}/medecin-traitant")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Modifier le médecin traitant")
    public ResponseEntity<AssureDTO.AssureResponse> updateMedecinTraitant(
            @PathVariable UUID id,
            @Valid @RequestBody AssureDTO.UpdateMedecinTraitantRequest request) {
        return ResponseEntity.ok(assureService.updateMedecinTraitant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Supprimer logiquement un assuré")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
