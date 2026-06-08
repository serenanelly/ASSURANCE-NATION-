package com.assurance.nation.mapper;

import com.assurance.nation.dto.PrescriptionDTO;
import com.assurance.nation.entity.Prescription;
import com.assurance.nation.entity.PrescriptionConsultation;
import com.assurance.nation.entity.PrescriptionMedicament;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionDTO.Response toResponse(Prescription p) {
        PrescriptionDTO.Response dto = new PrescriptionDTO.Response();
        dto.setId(p.getId());
        dto.setConsultationId(p.getConsultation().getId());
        dto.setType(p.getType());
        if (p instanceof PrescriptionMedicament pm) {
            dto.setMedicament(pm.getMedicament());
            dto.setPosologie(pm.getPosologie());
            dto.setDuree(pm.getDuree());
            dto.setNotes(pm.getNotes());
        } else if (p instanceof PrescriptionConsultation pc) {
            if (pc.getMedecinSpecialiste() != null) {
                dto.setMedecinSpecialisteId(pc.getMedecinSpecialiste().getId());
            }
            dto.setMotif(pc.getMotif());
            dto.setPriorite(pc.getPriorite());
            dto.setCodeReference(pc.getCodeReference());
        }
        return dto;
    }
}
