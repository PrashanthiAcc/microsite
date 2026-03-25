package com.ix.manufacturinglab.service;


import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserRequestDTO;


import java.util.List;

public interface UserService {

    List<UserManagementDTO> getAllActiveUsers();
    void createUser(UserRequestDTO dto);

}