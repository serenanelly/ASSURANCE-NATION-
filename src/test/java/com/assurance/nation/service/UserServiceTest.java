package com.assurance.nation.service;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.Assureur;
import com.assurance.nation.entity.Role;
import com.assurance.nation.entity.enums.RoleName;
import com.assurance.nation.entity.enums.UserType;
import com.assurance.nation.exception.UnauthorizedException;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.mapper.UserMapper;
import com.assurance.nation.repository.UserRepository;
import com.assurance.nation.security.OwnershipService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;
    @Mock private OwnershipService ownershipService;
    @InjectMocks private UserService userService;

    private Assureur user;
    private UUID id;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", "x", java.util.List.of()));
        id = UUID.randomUUID();
        user = new Assureur();
        user.setId(id);
        user.setEmail("test@test.com");
        user.setPasswordHash("encoded-old");
        user.setNom("Test");
        user.setPrenom("User");
        user.setUserType(UserType.ASSUREUR);
        Role role = Role.builder().roleName(RoleName.ADMIN).build();
        user.setRoles(Set.of(role));
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findById_returnsUser_whenOwnershipGranted() {
        when(ownershipService.canAccessUser(id)).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserDTO.UserResponse());
        assertThat(userService.findById(id)).isNotNull();
    }

    @Test
    void findById_denied_whenOwnershipFails() {
        when(ownershipService.canAccessUser(id)).thenReturn(false);
        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void findAll_returnsPage() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toResponse(user)).thenReturn(new UserDTO.UserResponse());
        assertThat(userService.findAll(0, 10).getContent()).hasSize(1);
    }

    @Test
    void update_changesEmail() {
        UserDTO.UpdateUserRequest request = new UserDTO.UpdateUserRequest();
        request.setEmail("new@test.com");
        UserDTO.UserResponse oldResponse = new UserDTO.UserResponse();
        oldResponse.setEmail("test@test.com");
        UserDTO.UserResponse newResponse = new UserDTO.UserResponse();
        newResponse.setEmail("new@test.com");

        when(ownershipService.canModifyUser(id)).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userMapper.toResponse(user)).thenReturn(oldResponse, newResponse);
        when(userRepository.save(user)).thenAnswer(inv -> inv.getArgument(0));

        UserDTO.UserResponse result = userService.update(id, request, "127.0.0.1");

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        verify(auditService).log(eq("User"), eq(id.toString()), any(), any(), any(), any(), eq("127.0.0.1"));
    }

    @Test
    void changePassword_updatesHash_whenConfirmMatches() {
        UserDTO.ChangePasswordRequest request = new UserDTO.ChangePasswordRequest();
        request.setCurrentPassword("OldPass1!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        when(ownershipService.canModifyUser(id)).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded-new");

        userService.changePassword(id, request);

        assertThat(user.getPasswordHash()).isEqualTo("encoded-new");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_rejectsMismatchedConfirm() {
        UserDTO.ChangePasswordRequest request = new UserDTO.ChangePasswordRequest();
        request.setCurrentPassword("OldPass1!");
        request.setNewPassword("NewPass1!");
        request.setConfirmPassword("Other1!");

        when(ownershipService.canModifyUser(id)).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(id, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void softDelete_setsDeletedAt() {
        when(ownershipService.canDeleteUser(id)).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserDTO.UserResponse());

        userService.softDelete(id, "127.0.0.1");

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).save(user);
        verify(auditService).log(eq("User"), eq(id.toString()), any(), any(), any(), isNull(), eq("127.0.0.1"));
    }
}
