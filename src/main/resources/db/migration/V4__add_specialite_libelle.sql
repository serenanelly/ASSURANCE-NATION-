-- Libellé libre de la spécialité du médecin (ex. "Cardiologie"), renseigné pour les spécialistes.
ALTER TABLE medecins ADD COLUMN specialite_libelle VARCHAR(150);
