package com.assurance.nation.service;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.AuditLog;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.repository.AuditLogRepository;
import com.assurance.nation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks private AuditService auditService;

    @Test
    void log_persistsAuditEntry() throws Exception {
        UUID userId = UUID.randomUUID();
        Assureur actor = new Assureur();
        actor.setId(userId);
        actor.setEmail("admin@test.com");
        UserDTO.UserResponse oldValue = new UserDTO.UserResponse();
        oldValue.setEmail("old@test.com");
        UserDTO.UserResponse newValue = new UserDTO.UserResponse();
        newValue.setEmail("new@test.com");

        when(objectMapper.writeValueAsString(oldValue)).thenReturn("{\"email\":\"old@test.com\"}");
        when(objectMapper.writeValueAsString(newValue)).thenReturn("{\"email\":\"new@test.com\"}");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        auditService.log("User", userId.toString(), AuditAction.UPDATE, actor, oldValue, newValue, "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo("User");
        assertThat(saved.getEntityId()).isEqualTo(userId.toString());
        assertThat(saved.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(saved.getUser()).isEqualTo(actor);
        assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(saved.getOldValue()).contains("old@test.com");
        assertThat(saved.getNewValue()).contains("new@test.com");
    }

    @Test
    void findActorByEmail_returnsUser() {
        Assureur u = new Assureur();
        when(userRepository.findByEmail("a@test.com")).thenReturn(java.util.Optional.of(u));
        assertThat(auditService.findActorByEmail("a@test.com")).isEqualTo(u);
    }

    @Test
    void toPage_mapsFields() {
        AuditLog log = AuditLog.builder().entityType("User").build();
        org.springframework.data.domain.Page<AuditLog> page =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(log));
        UserDTO.PageResponse<AuditLog> result = auditService.toPage(page);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
