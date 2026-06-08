package com.assurance.nation.service;

import com.assurance.nation.dto.AssureDTO;
import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.exception.DuplicateResourceException;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.repository.AssureRepository;
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
public class AssureService {

    private final AssureRepository assureRepository;
    private final MedecinService medecinService;
    private final PasswordEncoder passwordEncoder;
    private final RoleAssignmentService roleAssignmentService;

    @Transactional
    public AssureDTO.AssureResponse register(AssureDTO.RegisterAssureRequest request) {
        ValidationUtil.validateNSS(request.getNumSecuriteSociale());
        if (assureRepository.existsByNumSecuriteSociale(request.getNumSecuriteSociale())) {
            throw new DuplicateResourceException("Numéro de sécurité sociale déjà utilisé");
        }
        Assure assure = buildAssure(request);
        roleAssignmentService.assignRole(assure, RoleName.PATIENT);
        assure = assureRepository.save(assure);
        return toResponse(assure);
    }

    Assure createFromRegister(AuthDTO.RegisterRequest request) {
        ValidationUtil.validateNSS(request.getNumSecuriteSociale());
        if (request.getNumSecuriteSociale() == null) {
            throw new com.assurance.nation.exception.ValidationException("Numéro de sécurité sociale obligatoire");
        }
        if (assureRepository.existsByNumSecuriteSociale(request.getNumSecuriteSociale())) {
            throw new DuplicateResourceException("Numéro de sécurité sociale déjà utilisé");
        }
        Assure assure = new Assure();
        assure.setEmail(request.getEmail());
        assure.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        assure.setNom(request.getNom());
        assure.setPrenom(request.getPrenom());
        assure.setDateNaissance(request.getDateNaissance());
        assure.setLieuNaissance(request.getLieuNaissance());
        assure.setAdresse(request.getAdresse());
        assure.setTelephone(request.getTelephone());
        assure.setSexe(request.getSexe());
        assure.setNumSecuriteSociale(request.getNumSecuriteSociale());
        assure.setDateAffiliation(request.getDateAffiliation());
        assure.setEmploi(request.getEmploi());
        assure.setEstActif(true);
        roleAssignmentService.assignRole(assure, RoleName.PATIENT);
        return assure;
    }

    private Assure buildAssure(AssureDTO.RegisterAssureRequest request) {
        Assure assure = new Assure();
        assure.setEmail(request.getEmail());
        assure.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        assure.setNom(request.getNom());
        assure.setPrenom(request.getPrenom());
        assure.setNumSecuriteSociale(request.getNumSecuriteSociale());
        assure.setDateAffiliation(request.getDateAffiliation());
        assure.setEmploi(request.getEmploi());
        assure.setEstActif(true);
        if (request.getMedecinTraitantId() != null) {
            Medecin medecin = medecinService.getEntity(request.getMedecinTraitantId());
            ValidationUtil.validateMedecinTraitant(medecin);
            assure.setMedecinTraitant(medecin);
        }
        return assure;
    }

    @Transactional(readOnly = true)
    public PageDTO<AssureDTO.AssureResponse> findAll(int page, int size, String search, Boolean actif) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by("nom", "prenom"));
        Page<Assure> result = assureRepository.search(
                search != null && !search.isBlank() ? search : null, actif, pageable);
        return PageDTO.of(result, result.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public AssureDTO.AssureResponse findById(UUID id) {
        Assure assure = assureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable"));
        return toResponse(assure);
    }

    @Transactional
    public AssureDTO.AssureResponse update(UUID id, AssureDTO.UpdateAssureRequest request) {
        Assure assure = assureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable"));
        if (request.getNom() != null) assure.setNom(request.getNom());
        if (request.getPrenom() != null) assure.setPrenom(request.getPrenom());
        if (request.getEmploi() != null) assure.setEmploi(request.getEmploi());
        if (request.getEstActif() != null) assure.setEstActif(request.getEstActif());
        assure = assureRepository.save(assure);
        return toResponse(assure);
    }

    @Transactional
    public AssureDTO.AssureResponse updateMedecinTraitant(UUID id, AssureDTO.UpdateMedecinTraitantRequest request) {
        Assure assure = assureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable"));
        Medecin medecin = medecinService.getEntity(request.getMedecinTraitantId());
        ValidationUtil.validateMedecinTraitant(medecin);
        assure.setMedecinTraitant(medecin);
        assure = assureRepository.save(assure);
        return toResponse(assure);
    }

    @Transactional
    public void delete(UUID id) {
        Assure assure = assureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable"));
        assure.softDelete();
        assureRepository.save(assure);
    }

    @Transactional(readOnly = true)
    public AssureDTO.AssureResponse searchByNSS(String nss) {
        ValidationUtil.validateNSS(nss);
        Assure assure = assureRepository.findByNumSecuriteSociale(nss)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable pour ce numéro NSS"));
        return toResponse(assure);
    }

    @Transactional(readOnly = true)
    public Assure getEntity(UUID id) {
        return assureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assuré introuvable"));
    }

    private AssureDTO.AssureResponse toResponse(Assure a) {
        AssureDTO.AssureResponse dto = new AssureDTO.AssureResponse();
        dto.setId(a.getId());
        dto.setEmail(a.getEmail());
        dto.setNom(a.getNom());
        dto.setPrenom(a.getPrenom());
        dto.setNumSecuriteSociale(a.getNumSecuriteSociale());
        dto.setDateAffiliation(a.getDateAffiliation());
        dto.setEmploi(a.getEmploi());
        dto.setEstActif(a.isEstActif());
        if (a.getMedecinTraitant() != null) {
            dto.setMedecinTraitantId(a.getMedecinTraitant().getId());
        }
        return dto;
    }
}
