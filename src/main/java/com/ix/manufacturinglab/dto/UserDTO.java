package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String actionType;
    private String userEid;
    private String name;
    private String role;
    private String reason;
    private String creatorEId;
    private String approverEid;
    private String updaterEid;

}
