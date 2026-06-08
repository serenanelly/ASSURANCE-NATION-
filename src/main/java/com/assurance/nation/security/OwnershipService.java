package com.assurance.nation.security;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.User;
import com.assurance.nation.exception.UnauthorizedException;
import com.assurance.nation.repository.ConsultationRepository;
import com.assurance.nation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Contrôle d'accès aux ressources selon le rôle et la propriété (médecin / patient).
 */
@Service("ownershipService")
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final RoleChecker roleChecker;

    public void assertCanAccessAssure(UUID assureId) {
        if (roleChecker.isAdmin() || roleChecker.isAssureur()) {
            return;
        }
        if (roleChecker.isPatient()) {
            Assure current = getCurrentAssure();
            if (!current.getId().equals(assureId)) {
                throw new UnauthorizedException("Accès refusé à cet assuré");
            }
            return;
        }
        if (roleChecker.isMedecin()) {
            Medecin medecin = getCurrentMedecin();
            if (!consultationRepository.existsByMedecinIdAndAssureId(medecin.getId(), assureId)) {
                throw new UnauthorizedException("Accès refusé : patient non suivi");
            }
            return;
        }
        throw new UnauthorizedException("Accès refusé");
    }

    public void assertCanAccessConsultation(Consultation consultation) {
        if (roleChecker.isAdmin() || roleChecker.isAssureur()) {
            return;
        }
        if (roleChecker.isMedecin()) {
            Medecin medecin = getCurrentMedecin();
            if (!consultation.getMedecin().getId().equals(medecin.getId())) {
                throw new UnauthorizedException("Accès refusé à cette consultation");
            }
            return;
        }
        if (roleChecker.isPatient()) {
            Assure assure = getCurrentAssure();
            if (!consultation.getAssure().getId().equals(assure.getId())) {
                throw new UnauthorizedException("Accès refusé à cette consultation");
            }
            return;
        }
        throw new UnauthorizedException("Accès refusé");
    }

    public void assertCanAccessMedicalRecord(MedicalRecord record) {
        assertCanAccessAssure(record.getAssure().getId());
    }

    public void assertCanAccessReimbursement(Reimbursement reimbursement) {
        assertCanAccessMedicalRecord(reimbursement.getMedicalRecord());
    }

    public Medecin getCurrentMedecin() {
        return userRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .filter(Medecin.class::isInstance)
                .map(Medecin.class::cast)
                .orElseThrow(() -> new UnauthorizedException("Profil médecin requis"));
    }

    public Assure getCurrentAssure() {
        return userRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .filter(Assure.class::isInstance)
                .map(Assure.class::cast)
                .orElseThrow(() -> new UnauthorizedException("Profil patient requis"));
    }

    public User getCurrentUser() {
        return userRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }

    /** Expression SpEL : accès au profil utilisateur (soi-même ou ADMIN). */
    public boolean canAccessUser(UUID userId) {
        if (roleChecker.isAdmin()) {
            return true;
        }
        return getCurrentUser().getId().equals(userId);
    }

    /** Expression SpEL : modification du profil (soi-même ou ADMIN). */
    public boolean canModifyUser(UUID userId) {
        return canAccessUser(userId);
    }

    /** Expression SpEL : suppression logique (soi-même ou ADMIN). */
    public boolean canDeleteUser(UUID userId) {
        return canAccessUser(userId);
    }
}
