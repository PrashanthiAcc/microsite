package com.ix.manufacturinglab.exception;

import java.io.Serial;

public class CommonException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 5972727066746242539L;
    /**
     * Represents the error code associated with an operation or exception.
     * This code provides a standardized way to identify the nature of the error.
     */
    private final String errorCode;
    /**
     * Provides a descriptive message or explanation for a specific error or exception.
     * This description offers more details about the nature or cause of the error.
     */
    private final String errorDescription;
    /**
     * Constructs a new SapException Object with specified parameters errorCode, errorDescription.
     * @param errorCode gives the respective error code
     * @param errorDescription gives the respective error description
     */
    public CommonException(String errorCode, String errorDescription) {
        super(errorDescription);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
    /**
     * This method returns the respective error code occurred during the application flow.
     * @return errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }
    /**
     * This method returns the respective error description occurred during the application flow.
     * @return errorDescription is the description of the specific error
     */
    public String getErrorDescription() {
        return errorDescription;
    }
}
