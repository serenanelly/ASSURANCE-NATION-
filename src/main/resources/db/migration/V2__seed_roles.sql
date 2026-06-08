INSERT INTO roles (id, role_name, description) VALUES
    ('11111111-1111-1111-1111-111111111101', 'ADMIN', 'Administrateur système'),
    ('11111111-1111-1111-1111-111111111102', 'MEDECIN', 'Médecin prescripteur'),
    ('11111111-1111-1111-1111-111111111103', 'ASSUREUR', 'Agent de caisse / assurance'),
    ('11111111-1111-1111-1111-111111111104', 'PATIENT', 'Assuré / patient')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO permissions (id, permission_name, description) VALUES
    ('22222222-2222-2222-2222-222222222201', 'USER_READ', 'Lecture utilisateurs'),
    ('22222222-2222-2222-2222-222222222202', 'USER_WRITE', 'Écriture utilisateurs'),
    ('22222222-2222-2222-2222-222222222203', 'CONSULTATION_WRITE', 'Gestion consultations'),
    ('22222222-2222-2222-2222-222222222204', 'REIMBURSEMENT_WRITE', 'Gestion remboursements')
ON CONFLICT (permission_name) DO NOTHING;
