package com.ix.manufacturinglab.controller;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.common.exception.CommonErrorManagement;
import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserDTO;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UseCaseController.class);
    private final UserService userService;
    private final CommonErrorManagement errorResponse;

    public UserController(UserService userService, CommonErrorManagement errorResponse) {
        this.userService = userService;
        this.errorResponse = errorResponse;
    }

    /**
     * Fetch all active users
     */
    @GetMapping("/v1/all")
    public ResponseEntity<Object> getAllActiveUsers() {

        logger.debug("Received request to fetch all active users");

        try {
            List<UserManagementDTO> users = userService.getAllActiveUsers();

            return new ResponseEntity<>(users, HttpStatus.OK);

        } catch (CommonException e) {
            logger.error("Exception occurred while fetching use cases: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_USER_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/v1/create")
    public ResponseEntity<Object> createUser(@RequestBody UserDTO dto) {

        logger.debug("Received request to create user.");
        try {
            userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        }catch (CommonException e) {
            logger.error("Exception occurred while creating industry: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.CREATE_USER_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PutMapping("/v1/{userEid}/accept")
    public ResponseEntity<Object> acceptUser(@PathVariable String userEid,
                                             @RequestBody UserDTO dto) {
        logger.debug("Received request to accept user. userEid={}", userEid);
        try {
            userService.acceptUser(userEid, dto);
            return ResponseEntity.ok("User accepted successfully");
        } catch (CommonException e) {
            logger.error("Exception occurred while accepting user: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;
            try { status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode())); }
            catch (Exception ex) { status = HttpStatus.INTERNAL_SERVER_ERROR; }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    @PutMapping("/v1/{userEid}")
    public ResponseEntity<Object> updateUser(@PathVariable String userEid,
                                             @RequestBody UserDTO dto) {
        logger.debug("Received request to update user. userEid={}", userEid);
        try {
            userService.updateUser(userEid, dto);
            return ResponseEntity.ok("User updated successfully");
        } catch (CommonException e) {
            logger.error("Exception occurred while updating user: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;
            try { status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode())); }
            catch (Exception ex) { status = HttpStatus.INTERNAL_SERVER_ERROR; }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    @DeleteMapping("/v1/{userEid}/reject")
    public ResponseEntity<Object> deleteUser(@PathVariable String userEid) {
        logger.debug("Received request to delete user. userEid={}", userEid);
        try {
            userService.deleteUser(userEid);
            return ResponseEntity.ok("User deleted successfully");
        } catch (CommonException e) {
            logger.error("Exception occurred while deleting user: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;
            try { status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode())); }
            catch (Exception ex) { status = HttpStatus.INTERNAL_SERVER_ERROR; }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/v1/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable Integer userId) {

        logger.debug("Received request to delete user. userId={}", userId);

        try {
            userService.removeUser(userId);
            return ResponseEntity.ok("User soft deleted and related use cases archived");

        } catch (CommonException e) {
            logger.error("Exception occurred while removing user: {}", e.getMessage(), e);

            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;
            try {
                status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode()));
            } catch (Exception ex) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}