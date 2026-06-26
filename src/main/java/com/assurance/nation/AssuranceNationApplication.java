package com.assurance.nation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Point d'entrée de l'application ASSURANCE NATION.
 * Plateforme de gestion des consultations, prescriptions et remboursements
 * pour un organisme de sécurité sociale.
 */
@SpringBootApplication
@EnableAsync
public class AssuranceNationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssuranceNationApplication.class, args);
    }
}
