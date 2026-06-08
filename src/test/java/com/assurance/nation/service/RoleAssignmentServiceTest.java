package com.assurance.nation.service;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    @Mock private RoleRepository roleRepository;
    @InjectMocks private RoleAssignmentService roleAssignmentService;

    @Test
    void assignRole_setsUserRole() {
        Role patientRole = Role.builder().roleName(RoleName.PATIENT).description("Patient").build();
        when(roleRepository.findByRoleName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));

        Assure assure = new Assure();
        roleAssignmentService.assignRole(assure, RoleName.PATIENT);

        assertThat(assure.getRoles()).hasSize(1);
        assertThat(assure.getRoles().iterator().next().getRoleName()).isEqualTo(RoleName.PATIENT);
    }

    @Test
    void assignRole_unknownRole_throws() {
        when(roleRepository.findByRoleName(RoleName.MEDECIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleAssignmentService.assignRole(new Assure(), RoleName.MEDECIN))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Rôle introuvable");
    }
}
