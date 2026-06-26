package com.assurance.nation.util;

/**
 * Constantes globales de l'application ASSURANCE NATION.
 */
public final class Constants {

    private Constants() {}

    public static final String API_V1 = "/api/v1";
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int TAUX_GENERALISTE = 100;
    public static final int TAUX_SPECIALISTE = 80;

    public static final String REMBOURSEMENT_PREFIX = "RB";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
}
