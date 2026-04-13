package com.ix.common.exception;

import org.springframework.stereotype.Component;

/**
 * A class used to build Error Obejct that contains errorCode and errorDescription
 */
@Component
public class CommonErrorManagement {
    private String errorCode;
    private String errorDescription;

    /**
     * Constructs a new ErrorManagement Object with the specified errorDescription
     *
     * @param errorDescription - error description to populate
     */
    public CommonErrorManagement(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * Constructs a new ErrorManagement Object with the specified errorCode and errorDescription
     *
     * @param errorCode        -  error code to populate
     * @param errorDescription -  error description to populate
     */
    public CommonErrorManagement(String errorCode, String errorDescription) {
        super();
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    /**
     * Constructs a new ErrorManagement Object
     */
    public CommonErrorManagement() {
        super();
    }

    /**
     * Gets the errorCode.
     *
     * @return current errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the errorCode.
     *
     * @param errorCode to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


    /**
     * Gets the errorDescription.
     *
     * @return current errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Sets the errorDescription.
     *
     * @param errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}

