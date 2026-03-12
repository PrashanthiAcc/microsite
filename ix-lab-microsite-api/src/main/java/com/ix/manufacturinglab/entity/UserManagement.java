package com.ix.manufacturinglab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a User in the Manufacturing Lab Microsite.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_management", schema = "mfg")
public class UserManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_eid", nullable = false, length = 100)
    private String userEid;

    @Column(name = "is_presenter", nullable = false)
    private Boolean isPresenter;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin;

    @Column(name = "is_superadmin", nullable = false)
    private Boolean isSuperadmin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
