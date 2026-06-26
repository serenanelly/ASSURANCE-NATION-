#!/usr/bin/env bash
# Linux equivalent of run.cmd : backend ASSURANCE NATION (profil dev / PostgreSQL, port 8081).
set -e
cd "$(dirname "$0")"
# JDK 17 requis (Spring Boot 3.2.5 ne tourne pas sous Java 25+).
: "${JAVA_HOME:=$HOME/.jdks/jdk-17.0.19+10}"
export JAVA_HOME
export SPRING_PROFILES_ACTIVE=dev
exec ./mvnw spring-boot:run "$@"
