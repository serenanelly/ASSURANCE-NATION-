package com.assurance.nation.security;

import com.assurance.nation.util.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Vérification des rôles pour les expressions {@code @PreAuthorize}.
 */
@Component("roleChecker")
public class RoleChecker {

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isAssureur() {
        return hasRole("ASSUREUR");
    }

    public boolean isMedecin() {
        return hasRole("MEDECIN");
    }

    public boolean isPatient() {
        return hasRole("PATIENT");
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String authority = Constants.ROLE_PREFIX + role;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }
}
