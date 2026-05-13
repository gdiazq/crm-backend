package com.crm.mcsv_rrhh.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RequestStatus {
    PENDING_REVIEW("Pendiente de revisión"),
    PENDING_APPROVAL("Pendiente de aprobación"),
    APPROVED("Aprobado"),
    REJECTED("Rechazado");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Set<String> displayNamesOf(RequestStatus... values) {
        return Arrays.stream(values)
                .map(RequestStatus::getDisplayName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
