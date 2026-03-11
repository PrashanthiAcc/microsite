package com.ix.manufacturinglab.enums;

/**
 * Enum representing the type of an Artifact.
 */
public enum ArtifactType {
    ONE_SLIDER("one_slider"),
    MULTI_SLIDER("multi_slider"),
    DEMO_VIDEO("demo_video"),
    CLIENT_TESTIMONIAL("client_testimonial"),
    NARRATION("narration"),
    FAQ("faq");

    private final String displayName;

    ArtifactType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
