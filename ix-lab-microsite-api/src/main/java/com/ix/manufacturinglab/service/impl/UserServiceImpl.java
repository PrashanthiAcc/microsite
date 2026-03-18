package com.ix.manufacturinglab.service.impl;

import com.ix.manufacturinglab.dto.UserDTO;
import com.ix.manufacturinglab.entity.UserManagement;
import com.ix.manufacturinglab.repository.UserRepository;
import com.ix.manufacturinglab.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getAllActiveUsers() {

        List<UserManagement> users = userRepository.findByIsActiveTrue();

        return users.stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getUserEid(),
                        user.getIsPresenter(),
                        user.getIsAdmin(),
                        user.getIsSuperadmin()
                ))
                .toList();
    }
}
