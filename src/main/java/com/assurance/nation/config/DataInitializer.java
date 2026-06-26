package com.assurance.nation.config;

import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.entity.enums.UserType;
import com.assurance.nation.repository.RoleRepository;
import com.assurance.nation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialise les rôles et le compte admin si absents (profils dev/test).
 */
@Component
@Profile({"test", "dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        for (RoleName name : RoleName.values()) {
            roleRepository.findByRoleName(name).orElseGet(() -> roleRepository.save(Role.builder()
                    .roleName(name)
                    .description("Rôle " + name)
                    .build()));
        }
        if (userRepository.findByEmail("admin@assurance-nation.local").isEmpty()) {
            Assureur admin = new Assureur();
            admin.setEmail("admin@assurance-nation.local");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("Système");
            admin.setUserType(UserType.ASSUREUR);
            Role role = roleRepository.findByRoleName(RoleName.ADMIN).orElseThrow();
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            admin.setRoles(roles);
            userRepository.save(admin);
            log.info("Compte admin: admin@assurance-nation.local / admin123");
        }
    }
}
