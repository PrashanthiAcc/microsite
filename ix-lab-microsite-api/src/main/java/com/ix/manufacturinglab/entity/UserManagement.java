package com.ix.manufacturinglab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


    @Column(name = "name", length = 256)
    private String name;

    @Column(name = "role", length = 50)
    private String role;

    @Column(name = "access_start_date")
    private LocalDate accessStartDate;

    @Column(name = "requested_on")
    private LocalDate requestedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "user_id")
    private UserManagement approvedBy;

    @Column(name = "reason", length = 100)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "user_id")
    private UserManagement updatedBy;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
