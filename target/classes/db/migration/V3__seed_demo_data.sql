-- Données de démonstration (idempotent)
-- Mot de passe bcrypt pour "Password1!" (strength 12)

-- Médecin généraliste
INSERT INTO users (id, email, password_hash, nom, prenom, user_type, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111101',
        'medecin.demo@assurance-nation.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oX3vqK9pW5eO',
        'Martin', 'Jean', 'MEDECIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO medecins (id, numero_rpps, specialite, est_assure)
VALUES ('11111111-1111-1111-1111-111111111101', '12345678901', 'GENERALISTE', false)
ON CONFLICT (id) DO NOTHING;

-- Médecin spécialiste
INSERT INTO users (id, email, password_hash, nom, prenom, user_type, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111102',
        'specialiste.demo@assurance-nation.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oX3vqK9pW5eO',
        'Bernard', 'Sophie', 'MEDECIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO medecins (id, numero_rpps, specialite, est_assure)
VALUES ('11111111-1111-1111-1111-111111111102', '98765432109', 'SPECIALISTE', true)
ON CONFLICT (id) DO NOTHING;

-- Assurés (3)
INSERT INTO users (id, email, password_hash, nom, prenom, user_type, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222201',
        'patient.demo@assurance-nation.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oX3vqK9pW5eO',
        'Dupuis', 'Marie', 'ASSURE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO assures (id, num_securite_sociale, date_affiliation, emploi, medecin_traitant_id, est_actif)
VALUES ('22222222-2222-2222-2222-222222222201', '123456789012345', '2024-01-15', 'Ingénieur',
        '11111111-1111-1111-1111-111111111101', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, password_hash, nom, prenom, user_type, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222202',
        'patient2.demo@assurance-nation.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oX3vqK9pW5eO',
        'Leroy', 'Paul', 'ASSURE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO assures (id, num_securite_sociale, date_affiliation, emploi, medecin_traitant_id, est_actif)
VALUES ('22222222-2222-2222-2222-222222222202', '123456789012346', '2023-06-01', 'Comptable',
        '11111111-1111-1111-1111-111111111101', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, password_hash, nom, prenom, user_type, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222203',
        'patient3.demo@assurance-nation.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oX3vqK9pW5eO',
        'Petit', 'Anne', 'ASSURE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO assures (id, num_securite_sociale, date_affiliation, emploi, medecin_traitant_id, est_actif)
VALUES ('22222222-2222-2222-2222-222222222203', '123456789012347', '2025-02-10', 'Enseignant',
        '11111111-1111-1111-1111-111111111101', true)
ON CONFLICT (id) DO NOTHING;

-- Rôles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email IN ('medecin.demo@assurance-nation.local', 'specialiste.demo@assurance-nation.local')
  AND r.name = 'MEDECIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email LIKE 'patient%.demo@assurance-nation.local' AND r.name = 'PATIENT'
ON CONFLICT DO NOTHING;
