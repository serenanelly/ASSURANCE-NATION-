package com.assurance.nation.util;

import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.exception.ValidationException;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern RPPS_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern NSS_PATTERN = Pattern.compile("^\\d{15}$");

    private ValidationUtil() {}

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    public static void validateRPPS(String numeroRPPS) {
        if (numeroRPPS == null || !RPPS_PATTERN.matcher(numeroRPPS).matches()) {
            throw new ValidationException("Numéro RPPS invalide (11 chiffres requis)");
        }
    }

    public static void validateNSS(String numSecuriteSociale) {
        if (numSecuriteSociale == null || !NSS_PATTERN.matcher(numSecuriteSociale).matches()) {
            throw new ValidationException("Numéro de sécurité sociale invalide (15 chiffres requis)");
        }
    }

    public static void validatePasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            throw new ValidationException("Les mots de passe ne correspondent pas");
        }
    }

    public static void validateMedecinTraitant(Medecin medecin) {
        if (medecin.getSpecialite() != Specialite.GENERALISTE) {
            throw new ValidationException("Le médecin traitant doit être un généraliste");
        }
    }

    public static void validateDuree(String duree) {
        if (duree != null && duree.isBlank()) {
            throw new ValidationException("La durée de traitement ne peut pas être vide");
        }
    }
}
