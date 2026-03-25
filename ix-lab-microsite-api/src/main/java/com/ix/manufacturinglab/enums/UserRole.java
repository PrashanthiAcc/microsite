package com.ix.manufacturinglab.enums;

/**
 * Enum representing the role of user.
 */
public enum UserRole {
    PRESENTER("Presenter"),
    ADMIN("Admin"),
    SUPERADMIN("Super Admin");
    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
