package com.ix.manufacturinglab.enums;

/**
 * Enum representing the status of a Use Case.
 */
public enum UseCaseStatus {
    DRAFT("Draft"),
    IN_REVIEW("Under Review"),
    APPROVED("Approved"),
    ARCHIVED("Archived");

    private final String displayName;

    UseCaseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
