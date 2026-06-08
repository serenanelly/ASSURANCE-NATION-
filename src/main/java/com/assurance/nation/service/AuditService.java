package com.assurance.nation.service;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.AuditLog;
import com.assurance.nation.entity.User;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.repository.AuditLogRepository;
import com.assurance.nation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(String entityType, String entityId, AuditAction action,
                    User actor, Object oldValue, Object newValue, String ipAddress) {
        try {
            AuditLog log = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .user(actor)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // L'audit ne doit pas bloquer le flux métier
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Transactional(readOnly = true)
    public UserDTO.PageResponse<AuditLog> toPage(Page<AuditLog> page) {
        UserDTO.PageResponse<AuditLog> response = new UserDTO.PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public User findActorByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
