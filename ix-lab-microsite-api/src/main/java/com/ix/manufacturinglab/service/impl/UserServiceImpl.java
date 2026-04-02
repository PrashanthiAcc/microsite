package com.ix.manufacturinglab.service.impl;

import com.azure.core.exception.ResourceNotFoundException;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserDTO;
import com.ix.manufacturinglab.entity.UserManagement;
import com.ix.manufacturinglab.enums.UserRole;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.repository.UseCaseRepository;
import com.ix.manufacturinglab.repository.UserRepository;
import com.ix.manufacturinglab.service.UserService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UseCaseRepository useCaseRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, UseCaseRepository useCaseRepository ) {
        this.userRepository = userRepository;
        this.useCaseRepository = useCaseRepository;
    }

    @Override
    public List<UserManagementDTO> getAllActiveUsers() {

        List<UserManagement> users = userRepository.findByIsActiveTrue();

        return users.stream()
                .map(user -> {
                    UserManagementDTO dto = new UserManagementDTO();
                    dto.setUserId(user.getUserId());
                    dto.setUserEid(user.getUserEid());
                    dto.setName(user.getName());
                    dto.setRole(user.getRole() != null ? UserRole.valueOf(user.getRole().toUpperCase()) : null);
                    dto.setAccessStartDate(user.getAccessStartDate());
                    dto.setRequestedOn(user.getRequestedOn());
                    dto.setApprovedBy(user.getApprovedBy() != null ? user.getApprovedBy().getUserId() : null);
                    dto.setReason(user.getReason());
                    dto.setUpdatedBy(user.getUpdatedBy() != null ? user.getUpdatedBy().getUserId() : null);
                    dto.setLastUpdated(user.getLastUpdated());
                    dto.setIsActive(user.getIsActive());
                    dto.setApproverEid(user.getApprovedBy() != null ? user.getApprovedBy().getUserEid() : null);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void createUser(UserDTO dto) {

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
    @Override
    @Transactional
    public void acceptUser(String userEid, UserDTO dto) throws CommonException {
        try {
            UserManagement user = userRepository.findByUserEid(userEid.trim())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.USER_NOT_FOUND));

            UserManagement approver = userRepository.findByUserEid(dto.getApproverEid().trim())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.APPROVER_NOT_FOUND));

            user.setRole(dto.getRole().trim().toUpperCase());
            user.setAccessStartDate(LocalDate.now());
            user.setApprovedBy(approver);
            user.setUpdatedBy(approver);
            user.setIsActive(true);
            user.setLastUpdated(LocalDateTime.now());

            userRepository.save(user);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred while accepting user", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.ACCEPT_USER_GENERIC_ERROR_MESSAGE);
        }
    }
    @Override
    @Transactional
    public void updateUser(String userEid, UserDTO dto) throws CommonException {
        try {
            UserManagement user = userRepository.findByUserEid(userEid.trim())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.USER_NOT_FOUND));

            UserManagement updater = userRepository.findByUserEid(dto.getUpdaterEid().trim())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.UPDATER_NOT_FOUND));

            user.setRole(dto.getRole().trim().toUpperCase());
            user.setApprovedBy(updater);
            user.setUpdatedBy(updater);
            user.setLastUpdated(LocalDateTime.now());

            userRepository.save(user);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred while updating user", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.UPDATE_USER_GENERIC_ERROR_MESSAGE);
        }
    }

    @Override
    @Transactional
    public void deleteUser(String userEid) throws CommonException {
        try {
            UserManagement user = userRepository.findByUserEid(userEid.trim())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.USER_NOT_FOUND));

            userRepository.delete(user);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred while deleting user", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.DELETE_USER_GENERIC_ERROR_MESSAGE);
        }
    }

    @Transactional
    @Override
    public void removeUser(Integer userId) {

        try {
            UserManagement user = userRepository.findById(userId)
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            ManufacturingLabConstants.USER_NOT_FOUND));

            user.setIsActive(false);
            userRepository.save(user);

            useCaseRepository.archiveUseCasesByOwnerEid(user.getUserEid());

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception occurred while removing user", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.DELETE_USER_GENERIC_ERROR_MESSAGE);
        }
    }
}
