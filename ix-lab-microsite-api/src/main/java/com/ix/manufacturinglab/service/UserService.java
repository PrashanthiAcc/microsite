package com.ix.manufacturinglab.service;


import com.ix.manufacturinglab.dto.UserDTO;


import java.util.List;

public interface UserService {

    List<UserDTO> getAllActiveUsers();
}