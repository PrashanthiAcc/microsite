package com.ix.manufacturinglab.service.impl;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserRequestDTO;
import com.ix.manufacturinglab.entity.UserManagement;
import com.ix.manufacturinglab.enums.UserRole;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.repository.UserRepository;
import com.ix.manufacturinglab.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserManagementDTO> getAllActiveUsers() {

        List<UserManagement> users = userRepository.findByIsActiveTrue();

        return users.stream()
                .map(user -> new UserManagementDTO(
                        user.getUserId(),
                        user.getUserEid(),
                        user.getName(),
                        user.getRole() != null ? UserRole.valueOf(user.getRole().toUpperCase()) : null,
                        user.getAccessStartDate(),
                        user.getRequestedOn(),
                        user.getApprovedBy() != null ? user.getApprovedBy().getUserId() : null,
                        user.getReason(),
                        user.getUpdatedBy() != null ? user.getUpdatedBy().getUserId() : null,
                        user.getLastUpdated(),
                        user.getIsActive()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void createUser(UserRequestDTO dto) {

        String action = dto.getActionType().trim().toUpperCase();
        String userEid = dto.getUserEid().trim();

        if (userRepository.existsByUserEid(userEid)) {
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.USER_ALREADY_PRESENT);
        }

        if ("REQUEST_ACCESS".equals(action)) {


            UserManagement user = UserManagement.builder()
                    .userEid(userEid)
                    .name(dto.getName())
                    .role("PRESENTER")
                    .accessStartDate(null)
                    .requestedOn(LocalDate.now())
                    .approvedBy(null)
                    .reason(dto.getReason())
                    .updatedBy(null)
                    .isActive(false)
                    .lastUpdated(LocalDateTime.now())
                    .build();

            user = userRepository.save(user);

            user.setUpdatedBy(user);
            userRepository.save(user);

            return;
        }

        if ("ADD_USER".equals(action)) {

            String creatorEid = dto.getCreatorEId().trim();

            UserManagement creatorUser = userRepository.findByUserEid(creatorEid)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + creatorEid));

            UserManagement user = UserManagement.builder()
                    .userEid(userEid)
                    .name(dto.getName())
                    .role(dto.getRole())
                    .accessStartDate(LocalDate.now())
                    .requestedOn(null)
                    .approvedBy(creatorUser)
                    .reason("Added by " + creatorEid)
                    .updatedBy(creatorUser)
                    .lastUpdated(LocalDateTime.now())
                    .isActive(true)
                    .build();

            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid actionType");
        }
    }
}
