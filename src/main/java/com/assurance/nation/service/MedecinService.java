package com.assurance.nation.service;

import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.dto.MedecinDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.exception.DuplicateResourceException;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.repository.MedecinRepository;
import com.assurance.nation.util.Constants;
import com.assurance.nation.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedecinService {

    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleAssignmentService roleAssignmentService;

    @Transactional
    public MedecinDTO.MedecinResponse register(MedecinDTO.RegisterMedecinRequest request) {
        ValidationUtil.validateRPPS(request.getNumeroRPPS());
        if (medecinRepository.existsByNumeroRPPS(request.getNumeroRPPS())) {
            throw new DuplicateResourceException("Numéro RPPS déjà enregistré");
        }
        Medecin medecin = new Medecin();
        medecin.setEmail(request.getEmail());
        medecin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        medecin.setNom(request.getNom());
        medecin.setPrenom(request.getPrenom());
        medecin.setNumeroRPPS(request.getNumeroRPPS());
        medecin.setSpecialite(request.getSpecialite());
        medecin.setEstAssure(request.isEstAssure());
        medecin.setTelephone(request.getTelephone());
        roleAssignmentService.assignRole(medecin, RoleName.MEDECIN);
        medecin = medecinRepository.save(medecin);
        return toResponse(medecin);
    }

    Medecin createFromRegister(AuthDTO.RegisterRequest request) {
        ValidationUtil.validateRPPS(request.getNumeroRPPS());
        if (request.getNumeroRPPS() == null || request.getSpecialite() == null) {
            throw new com.assurance.nation.exception.ValidationException("RPPS et spécialité obligatoires pour un médecin");
        }
        if (medecinRepository.existsByNumeroRPPS(request.getNumeroRPPS())) {
            throw new DuplicateResourceException("Numéro RPPS déjà utilisé");
        }
        Medecin medecin = new Medecin();
        medecin.setEmail(request.getEmail());
        medecin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        medecin.setNom(request.getNom());
        medecin.setPrenom(request.getPrenom());
        medecin.setDateNaissance(request.getDateNaissance());
        medecin.setLieuNaissance(request.getLieuNaissance());
        medecin.setAdresse(request.getAdresse());
        medecin.setTelephone(request.getTelephone());
        medecin.setSexe(request.getSexe());
        medecin.setNumeroRPPS(request.getNumeroRPPS());
        medecin.setSpecialite(request.getSpecialite());
        roleAssignmentService.assignRole(medecin, RoleName.MEDECIN);
        return medecin;
    }

    @Transactional(readOnly = true)
    public PageDTO<MedecinDTO.MedecinResponse> findAll(int page, int size, String search, Specialite specialite) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by("nom", "prenom"));
        Page<Medecin> result = medecinRepository.search(
                search != null && !search.isBlank() ? search : null, specialite, pageable);
        return PageDTO.of(result, result.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public MedecinDTO.MedecinResponse findById(UUID id) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable"));
        return toResponse(medecin);
    }

    @Transactional
    public MedecinDTO.MedecinResponse update(UUID id, MedecinDTO.UpdateMedecinRequest request) {
        Medecin medecin = getEntity(id);
        if (request.getNom() != null) medecin.setNom(request.getNom());
        if (request.getPrenom() != null) medecin.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) medecin.setTelephone(request.getTelephone());
        if (request.getSpecialite() != null) medecin.setSpecialite(request.getSpecialite());
        if (request.getEstAssure() != null) medecin.setEstAssure(request.getEstAssure());
        medecin = medecinRepository.save(medecin);
        return toResponse(medecin);
    }

    @Transactional
    public void delete(UUID id) {
        Medecin medecin = getEntity(id);
        medecin.softDelete();
        medecinRepository.save(medecin);
    }

    @Transactional(readOnly = true)
    public MedecinDTO.MedecinResponse findByRPPS(String numeroRPPS) {
        ValidationUtil.validateRPPS(numeroRPPS);
        Medecin medecin = medecinRepository.findByNumeroRPPS(numeroRPPS)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable pour ce RPPS"));
        return toResponse(medecin);
    }

    @Transactional(readOnly = true)
    public Medecin getEntity(UUID id) {
        return medecinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable"));
    }

    private MedecinDTO.MedecinResponse toResponse(Medecin m) {
        MedecinDTO.MedecinResponse dto = new MedecinDTO.MedecinResponse();
        dto.setId(m.getId());
        dto.setEmail(m.getEmail());
        dto.setNom(m.getNom());
        dto.setPrenom(m.getPrenom());
        dto.setNumeroRPPS(m.getNumeroRPPS());
        dto.setSpecialite(m.getSpecialite());
        dto.setEstAssure(m.isEstAssure());
        return dto;
    }
}
