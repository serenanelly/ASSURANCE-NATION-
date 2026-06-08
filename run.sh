#!/usr/bin/env bash
# Démarre l'API ASSURANCE NATION (profil local H2 par défaut)
cd "$(dirname "$0")"
exec ./mvnw spring-boot:run "$@"
