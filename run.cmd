@echo off
REM Demarre le backend ASSURANCE NATION (profil dev / PostgreSQL, port 8081).
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "SPRING_PROFILES_ACTIVE=dev"
call "%~dp0mvnw.cmd" spring-boot:run
