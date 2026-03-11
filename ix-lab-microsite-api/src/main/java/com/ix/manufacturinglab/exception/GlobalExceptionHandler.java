package com.ix.manufacturinglab.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Manufacturing Lab Microsite API.
 * Provides centralized exception handling across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors on request body fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.BAD_REQUEST.value());

        String errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        errorBody.put("errorDescription", errorMessages);

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation errors on path/query parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.error("Constraint violation: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.BAD_REQUEST.value());

        String errorMessages = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        errorBody.put("errorDescription", errorMessages);

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        logger.error("Missing request parameter: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorBody.put("errorDescription", "Missing required parameter: " + ex.getParameterName());

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing required request headers.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        logger.error("Missing request header: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorBody.put("errorDescription", "Missing required header: " + ex.getHeaderName());

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch (e.g., passing string to a Long parameter).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.error("Argument type mismatch: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorBody.put("errorDescription", "Invalid value for parameter: " + ex.getName());

        return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
    }   

    /**
     * Handle all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("errorDescription", "An unexpected error occurred. Please try again later.");

        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
