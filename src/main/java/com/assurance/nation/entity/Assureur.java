package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Agent assureur (gestion des assurés et remboursements).
 */
@Entity
@Table(name = "assureurs")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("ASSUREUR")
@Getter
@Setter
@NoArgsConstructor
public class Assureur extends User {

    @PrePersist
    @PreUpdate
    void setType() {
        setUserType(UserType.ASSUREUR);
    }
}
