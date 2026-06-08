package com.assurance.nation.repository;

import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.User;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.entity.enums.UserType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private EntityManager entityManager;

    private Role adminRole;

    @BeforeEach
    void seedRole() {
        adminRole = roleRepository.save(Role.builder()
                .roleName(RoleName.ADMIN)
                .description("Admin")
                .build());
    }

    @Test
    void softDeletedUser_excludedFromFindById() {
        String email = "soft-delete@test.com";
        Assureur user = persistUser(email);
        UUID id = user.getId();

        user.softDelete();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.findByEmail(email)).isEmpty();

        Object deletedAt = entityManager.createNativeQuery(
                        "SELECT deleted_at FROM users WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
        assertThat(deletedAt).isNotNull();
    }

    @Test
    void activeUser_foundByEmail() {
        Assureur user = persistUser("active@test.com");

        Optional<User> found = userRepository.findByEmail("active@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    private Assureur persistUser(String email) {
        Assureur user = new Assureur();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setNom("Test");
        user.setPrenom("User");
        user.setUserType(UserType.ASSUREUR);
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
