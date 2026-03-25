package com.ix.manufacturinglab.controller;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.common.exception.CommonErrorManagement;
import com.ix.manufacturinglab.dto.UserManagementDTO;
import com.ix.manufacturinglab.dto.UserRequestDTO;
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

        logger.info("Received request to fetch all active users");

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
    public ResponseEntity<String> createUser(@RequestBody UserRequestDTO dto) {
        userService.createUser(dto);
        return ResponseEntity.ok("User created successfully");
    }
}