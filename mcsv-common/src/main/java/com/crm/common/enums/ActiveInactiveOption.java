package com.crm.common.enums;

public enum ActiveInactiveOption implements SelectableOption {
    ACTIVE(1L, true, "Activo"),
    INACTIVE(2L, false, "Inactivo");

    private final Long id;
    private final Boolean value;
    private final String displayName;

    ActiveInactiveOption(Long id, Boolean value, String displayName) {
        this.id = id;
        this.value = value;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public Boolean getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
