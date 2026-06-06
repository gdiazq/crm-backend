package com.crm.mcsv_user.enums;

public enum StatusOption {
    ACTIVE(true, "Activo"),
    INACTIVE(false, "Inactivo");

    private final Boolean value;
    private final String displayName;

    StatusOption(Boolean value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public Boolean getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
