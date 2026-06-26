package com.assurance.nation.service;

import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.User;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final RoleRepository roleRepository;

    public void assignRole(User user, RoleName roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ValidationException("Rôle introuvable: " + roleName));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }
}
