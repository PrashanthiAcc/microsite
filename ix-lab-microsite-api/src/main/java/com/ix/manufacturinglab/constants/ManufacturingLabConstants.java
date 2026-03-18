package com.ix.manufacturinglab.constants;

/**
 * Constants used across the Manufacturing Lab Microsite application.
 */
public final class ManufacturingLabConstants {

    private ManufacturingLabConstants() {
        // Prevent instantiation
    }

    // Header Constants
    public static final String HEADER_ACCESS_TOKEN = "Authorization";
    public static final String HEADER_TRANSACTION_ID = "transaction-id";

    // Status Constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_IN_REVIEW = "IN_REVIEW";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_ON_HOLD = "ON_HOLD";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    // Error Messages
    public static final String INDUSTRY_NOT_FOUND = "Industry not found";
    public static final String USE_CASE_NOT_FOUND = "Use case not found with id: ";
    public static final String USE_CASE_ALREADY_EXISTS = "Use case already exists with title: ";
    public static final String INVALID_STATUS_TRANSITION = "Invalid status transition from %s to %s";
    public static final String INVALID_USE_CASE_ID_ERROR_MESSAGE = "Please provide Valid Id for Use case";
    public static final String INVALID_CATEGORY = "Invalid category: ";
    public static final String INVALID_PRIORITY = "Invalid priority: ";
    public static final String INVALID_COMPLEXITY = "Invalid complexity: ";
    public static final String SUB_INDUSTRY_NOT_FOUND = "sub industry not found with id: ";
    public static final String INDUSTRY_ID_NOT_FOUND = "sub industry not found with industry id: ";

    public static final String DELETE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE = "An error occurred while deleting sub-industry";

    // Success Messages
    public static final String INDUSTRY_DELETED_SUCCESS = "Industry deleted successfully";
    public static final String USE_CASE_CREATED = "Use case created successfully";
    public static final String USE_CASE_UPDATED = "Use case updated successfully";
    public static final String USE_CASE_DELETED = "Use case deleted successfully";
    public static final String USE_CASE_STATUS_UPDATED = "Use case status updated successfully";
   // Failure Messages
    public static final String CREATE_INDUSTRY_GENERIC_ERROR_MESSAGE = "An Error occurred while creating industry";
    public static final String UPDATE_INDUSTRY_GENERIC_ERROR_MESSAGE = "Error occurred while updating industry";
    public static final String DELETE_INDUSTRY_GENERIC_ERROR_MESSAGE = "Error occurred while deleting industry";
    public static final String DRAFT_TITLE_REQUIRED = "An error occurred while Drafting the Use case. Required Title";
    public static final String SAVE_DRAFT_GENERIC_ERROR_MESSAGE = "An error occurred while drafting the use case";
    public static final String SUBMIT_FOR_APPROVAL_GENERIC_ERROR_MESSAGE = "An error occurred while sending for approval";
    public static final String UPDATE_USE_CASE_GENERIC_ERROR_MESSAGE = "An error occurred while updating the Use case";
    public static final String DELETE_USE_CASE_GENERIC_ERROR_MESSAGE = "An error occurred while deleting the Use case";
    public static final String SEARCH_USE_CASE_GENERIC_ERROR_MESSAGE = "An unexpected error occurred while searching Use case.";
    public static final String SEARCH_USER_GENERIC_ERROR_MESSAGE = "An unexpected error occurred while fetching user.";

    public static final String CREATE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE = "An error occurred while creating the Sub - Industry";

    public static final String UPDATE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE = "An error occurred while updating the Sub - Industry";

    // Pagination Defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Log Messages
    public static final String LOG_SAVING_DRAFT = "use case with title: {} saved as Draft";
    public static final String LOG_SUBMITTING_FOR_APPROVAL = "use case with title: {} sent for approval";
    public static final String LOG_UPDATING_USE_CASE = "Updating use case with id: {}";
    public static final String LOG_DELETING_USE_CASE = "Deleting use case with id: {}";
    public static final String LOG_FETCHING_USE_CASE = "Fetching use case with id: {}";
    public static final String LOG_SEARCHING_USE_CASES = "Searching use cases with keyword: {} and filters";

    public static final String SEARCH_INDUSTRY_GENERIC_ERROR_MESSAGE = "An unexpected error occurred while searching Industry.";

    public static final String SEARCH_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE = "An unexpected error occurred while searching Sub Industry.";
    public static final String SEARCH_VALUE_CHAIN_GENERIC_ERROR_MESSAGE = "An unexpected error occurred while searching value chain.";

    public static final String TRANSACTION_ID = "transaction-Id";
    public static final String PLATFORM_TRANSACTION_ID ="platform-transaction-Id";
    public static final String USER_ROLE = "user-role";
    public static final String USER_ROLE_ERROR = "User does not have a valid role";

}
