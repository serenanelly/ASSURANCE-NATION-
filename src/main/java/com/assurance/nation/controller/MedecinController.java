package com.assurance.nation.controller;

import com.assurance.nation.dto.MedecinDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.service.MedecinService;
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
@RequestMapping(Constants.API_V1 + "/users/medecins")
@RequiredArgsConstructor
@Tag(name = "Médecins", description = "Gestion des médecins")
@SecurityRequirement(name = "bearerAuth")
public class MedecinController {

    private final MedecinService medecinService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Enregistrer un médecin (ASSUREUR)")
    public ResponseEntity<MedecinDTO.MedecinResponse> register(@Valid @RequestBody MedecinDTO.RegisterMedecinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medecinService.register(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Lister les médecins (paginé, recherche)")
    public ResponseEntity<PageDTO<MedecinDTO.MedecinResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Specialite specialite) {
        return ResponseEntity.ok(medecinService.findAll(page, size, search, specialite));
    }

    @GetMapping(params = "rpps")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Rechercher un médecin par numéro RPPS")
    public ResponseEntity<MedecinDTO.MedecinResponse> findByRpps(@RequestParam String rpps) {
        return ResponseEntity.ok(medecinService.findByRPPS(rpps));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR','MEDECIN','PATIENT')")
    @Operation(summary = "Détails d'un médecin")
    public ResponseEntity<MedecinDTO.MedecinResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(medecinService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Modifier un médecin")
    public ResponseEntity<MedecinDTO.MedecinResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MedecinDTO.UpdateMedecinRequest request) {
        return ResponseEntity.ok(medecinService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSUREUR')")
    @Operation(summary = "Supprimer logiquement un médecin")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        medecinService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
