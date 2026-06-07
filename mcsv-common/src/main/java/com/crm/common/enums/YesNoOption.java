package com.crm.common.enums;

public enum YesNoOption implements SelectableOption {
    YES(1L, true, "Sí"),
    NO(2L, false, "No");

    private final Long id;
    private final Boolean value;
    private final String displayName;

    YesNoOption(Long id, Boolean value, String displayName) {
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
