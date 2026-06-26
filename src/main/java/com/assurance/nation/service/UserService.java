package com.assurance.nation.service;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.User;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.exception.DuplicateResourceException;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.mapper.UserMapper;
import com.assurance.nation.repository.UserRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.security.SecurityUtil;
import com.assurance.nation.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Gestion des comptes utilisateurs (CRUD, mot de passe, soft delete).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final OwnershipService ownershipService;

    @Transactional(readOnly = true)
    public UserDTO.PageResponse<UserDTO.UserResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<User> users = userRepository.findAll(pageable);
        List<UserDTO.UserResponse> content = users.getContent().stream().map(userMapper::toResponse).toList();
        UserDTO.PageResponse<UserDTO.UserResponse> response = new UserDTO.PageResponse<>();
        response.setContent(content);
        response.setPage(users.getNumber());
        response.setSize(users.getSize());
        response.setTotalElements(users.getTotalElements());
        response.setTotalPages(users.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public UserDTO.UserResponse findById(UUID id) {
        if (!ownershipService.canAccessUser(id)) {
            throw new com.assurance.nation.exception.UnauthorizedException("Accès refusé");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserDTO.UserResponse update(UUID id, UserDTO.UpdateUserRequest request, String ip) {
        if (!ownershipService.canModifyUser(id)) {
            throw new com.assurance.nation.exception.UnauthorizedException("Modification non autorisée");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        UserDTO.UserResponse old = userMapper.toResponse(user);
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getDateNaissance() != null) user.setDateNaissance(request.getDateNaissance());
        if (request.getLieuNaissance() != null) user.setLieuNaissance(request.getLieuNaissance());
        if (request.getAdresse() != null) user.setAdresse(request.getAdresse());
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
        if (request.getSexe() != null) user.setSexe(request.getSexe());
        if (request.getPhotoUrl() != null) user.setPhotoUrl(request.getPhotoUrl());
        user = userRepository.save(user);
        auditService.log("User", id.toString(), AuditAction.UPDATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), old, userMapper.toResponse(user), ip);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID id, UserDTO.ChangePasswordRequest request) {
        if (!ownershipService.canModifyUser(id)) {
            throw new com.assurance.nation.exception.UnauthorizedException("Modification non autorisée");
        }
        ValidationUtil.validatePasswordMatch(request.getNewPassword(), request.getConfirmPassword());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Mot de passe actuel incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void softDelete(UUID id, String ip) {
        if (!ownershipService.canDeleteUser(id)) {
            throw new com.assurance.nation.exception.UnauthorizedException("Suppression non autorisée");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.softDelete();
        userRepository.save(user);
        auditService.log("User", id.toString(), AuditAction.DELETE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), userMapper.toResponse(user), null, ip);
    }
}
