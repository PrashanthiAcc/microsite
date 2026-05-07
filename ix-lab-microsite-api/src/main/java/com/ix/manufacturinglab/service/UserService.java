package com.ix.manufacturinglab.service;


import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserDTO;


import java.util.List;

public interface UserService {

    List<UserManagementDTO> getAllActiveUsers();

    void createUser(UserDTO dto);

    void acceptUser(String userEid, UserDTO dto);

    void updateUser(String userEid, UserDTO dto);

    void deleteUser(String userEid);

    void removeUser(Integer userId);

    boolean checkPasswordExists(String userEid, String userPassword);


}