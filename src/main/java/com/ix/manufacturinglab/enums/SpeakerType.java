package com.ix.manufacturinglab.enums;

/**
 * Enum representing the type of a Speaker.
 */
public enum SpeakerType {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    TERTIARY("Tertiary");

    private final String displayName;

    SpeakerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
