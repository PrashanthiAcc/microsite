package com.ix.manufacturinglab.dto;


import com.ix.manufacturinglab.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    private Integer userId;
    private String userEid;
    private String name;
    private UserRole role;
    private LocalDate accessStartDate;
    private LocalDate requestedOn;
    private Integer approvedBy;
    private String reason;
    private Integer updatedBy;
    private LocalDateTime lastUpdated;
    private Boolean isActive;
}