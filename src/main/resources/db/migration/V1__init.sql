-- Schéma initial ASSURANCE NATION

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_type VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE,
    lieu_naissance VARCHAR(150),
    adresse VARCHAR(500),
    telephone VARCHAR(30),
    sexe VARCHAR(1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE medecins (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    numero_rpps VARCHAR(20) NOT NULL UNIQUE,
    specialite VARCHAR(20) NOT NULL,
    est_assure BOOLEAN DEFAULT FALSE
);

CREATE TABLE assures (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    num_securite_sociale VARCHAR(20) NOT NULL UNIQUE,
    date_affiliation DATE,
    emploi VARCHAR(150),
    medecin_traitant_id UUID REFERENCES medecins(id),
    est_actif BOOLEAN DEFAULT TRUE
);

CREATE TABLE assureurs (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE consultations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assure_id UUID NOT NULL REFERENCES assures(id),
    medecin_id UUID NOT NULL REFERENCES medecins(id),
    date_consultation TIMESTAMP NOT NULL,
    type_consultation VARCHAR(20) NOT NULL,
    diagnostique TEXT,
    motif TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_type VARCHAR(30) NOT NULL,
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE prescription_medicaments (
    id UUID PRIMARY KEY REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicament VARCHAR(200) NOT NULL,
    posologie VARCHAR(500),
    duree VARCHAR(100),
    notes TEXT
);

CREATE TABLE prescription_consultations (
    id UUID PRIMARY KEY REFERENCES prescriptions(id) ON DELETE CASCADE,
    medecin_specialiste_id UUID REFERENCES medecins(id),
    motif TEXT,
    priorite VARCHAR(20),
    code_reference VARCHAR(50)
);

CREATE TABLE medical_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assure_id UUID NOT NULL REFERENCES assures(id),
    medecin_id UUID NOT NULL REFERENCES medecins(id),
    consultation_id UUID NOT NULL UNIQUE REFERENCES consultations(id),
    date DATE NOT NULL,
    nom_maladie VARCHAR(255),
    est_remboursee BOOLEAN DEFAULT FALSE,
    date_remboursement TIMESTAMP,
    montant_rembourse NUMERIC(12,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE reimbursements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    num_remboursement VARCHAR(20) NOT NULL UNIQUE,
    medical_record_id UUID NOT NULL UNIQUE REFERENCES medical_records(id),
    montant_total NUMERIC(12,2) NOT NULL,
    taux_remboursement INTEGER NOT NULL,
    montant_rembourse NUMERIC(12,2) NOT NULL,
    mode_paiement VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    date_remboursement TIMESTAMP,
    justificatif_path VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    user_id UUID REFERENCES users(id),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_consultations_assure ON consultations(assure_id);
CREATE INDEX idx_consultations_medecin ON consultations(medecin_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_reimbursements_status ON reimbursements(status);
