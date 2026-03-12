package com.ix.manufacturinglab.constants;

/**
 * Commonly used status codes of Teamcenter Connector Application
 */
public class CommonExceptionConstants {
    /**
     * 200 SUCCESS
     */
    public static final String SUCCESS = "200";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    /**
     * Constant representing a successful status code.
     */
    public static final String DELETED_SUCCESS = "204";
    /**
     * Constant representing an invalid URL error code.
     */
    public static final String INVALID_URL_CODE = "001001";

    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String METHOD_NOT_ALLOWED = "405";
    public static final String NOT_ACCEPTABLE = "406";
    public static final String REQUEST_TIMEOUT = "408";
    public static final String CONFLICT = "409";
    /**
     * GONE-Resource does not exist any longer
     */
    public static final String GONE = "410";
    public static final String PRECONDITION_FAILED = "412";
    public static final String UNSUPPORTED_MEDIA_TYPE = "415";
    /**
     * Locked - Pessimistic locking, e.g. processing states
     */
    public static final String LOCKED = "423";
    public static final String PRECONDITION_REQUIRED = "428";
    public static final String TOO_MANY_REQUESTS = "429";
    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String NOT_IMPLEMENTED = "501";
    public static final String SERVICE_UNAVAILABLE = "503";

    private CommonExceptionConstants() {
    }

}



