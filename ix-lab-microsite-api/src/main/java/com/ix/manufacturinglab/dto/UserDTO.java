package com.ix.manufacturinglab.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer userId;
    private String userEid;
    private Boolean isPresenter;
    private Boolean isAdmin;
    private Boolean isSuperAdmin;
}