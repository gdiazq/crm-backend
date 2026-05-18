package com.crm.mcsv_recruitment.enums;

public enum JobOpeningStatusName {
    DRAFT("DRAFT"),
    OPEN("OPEN"),
    ON_HOLD("ON_HOLD"),
    FILLED("FILLED"),
    CANCELLED("CANCELLED");

    private final String displayName;

    JobOpeningStatusName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
