package com.assurance.nation.service;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.security.OwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.assurance.nation.dto.MedicalRecordDTO;
import com.assurance.nation.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private OwnershipService ownershipService;
    @InjectMocks private MedicalRecordService medicalRecordService;

    private MedicalRecord sampleRecord(UUID id) {
        Assure assure = new Assure();
        assure.setId(UUID.randomUUID());
        Medecin medecin = new Medecin();
        medecin.setId(UUID.randomUUID());
        Consultation consultation = Consultation.builder().id(UUID.randomUUID()).build();
        return MedicalRecord.builder()
                .id(id)
                .assure(assure)
                .medecin(medecin)
                .consultation(consultation)
                .date(LocalDate.now())
                .build();
    }

    @Test
    void findByAssureId() {
        UUID assureId = UUID.randomUUID();
        doNothing().when(ownershipService).assertCanAccessAssure(assureId);
        when(medicalRecordRepository.findByAssureFiltered(eq(assureId), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleRecord(UUID.randomUUID()))));
        assertThat(medicalRecordService.findByAssureId(assureId, 0, 20, null, null, null).getContent()).hasSize(1);
    }

    @Test
    void findById() {
        UUID id = UUID.randomUUID();
        MedicalRecord record = sampleRecord(id);
        when(medicalRecordRepository.findById(id)).thenReturn(Optional.of(record));
        doNothing().when(ownershipService).assertCanAccessMedicalRecord(record);
        assertThat(medicalRecordService.findById(id).getId()).isEqualTo(id);
    }

    @Test
    void update_modifiesNomMaladie() {
        UUID id = UUID.randomUUID();
        MedicalRecord record = sampleRecord(id);
        record.setNomMaladie("Grippe");
        MedicalRecordDTO.UpdateRequest request = new MedicalRecordDTO.UpdateRequest();
        request.setNomMaladie("Angine");

        when(medicalRecordRepository.findById(id)).thenReturn(Optional.of(record));
        doNothing().when(ownershipService).assertCanAccessMedicalRecord(record);
        when(medicalRecordRepository.save(record)).thenAnswer(inv -> inv.getArgument(0));

        MedicalRecordDTO.Response response = medicalRecordService.update(id, request);

        assertThat(response.getNomMaladie()).isEqualTo("Angine");
        verify(medicalRecordRepository).save(record);
    }

    @Test
    void update_blockedWhenRemboursee() {
        UUID id = UUID.randomUUID();
        MedicalRecord record = sampleRecord(id);
        record.setEstRemboursee(true);
        MedicalRecordDTO.UpdateRequest request = new MedicalRecordDTO.UpdateRequest();
        request.setNomMaladie("Angine");

        when(medicalRecordRepository.findById(id)).thenReturn(Optional.of(record));
        doNothing().when(ownershipService).assertCanAccessMedicalRecord(record);

        assertThatThrownBy(() -> medicalRecordService.update(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("non modifiable");
    }
}
