package com.assurance.nation.mapper;

import com.assurance.nation.dto.UserDTO;
import com.assurance.nation.entity.User;
import com.assurance.nation.util.Constants;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO.UserResponse toResponse(User user) {
        UserDTO.UserResponse dto = new UserDTO.UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setDateNaissance(user.getDateNaissance());
        dto.setLieuNaissance(user.getLieuNaissance());
        dto.setAdresse(user.getAdresse());
        dto.setTelephone(user.getTelephone());
        dto.setSexe(user.getSexe());
        dto.setUserType(user.getUserType());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(r -> Constants.ROLE_PREFIX + r.getRoleName().name())
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
