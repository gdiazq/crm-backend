package com.crm.mcsv_rrhh.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RequestStatus {
    PENDING_REVIEW("Pendiente de revisión"),
    PENDING_APPROVAL("Pendiente de aprobación"),
    APPROVED("Aprobado"),
    REJECTED("Rechazado"),
    SYNC_ERROR("Error de sincronización");

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

    public static final Set<String> ACTIVE_DISPLAY_NAMES = displayNamesOf(
            PENDING_REVIEW, PENDING_APPROVAL, APPROVED
    );
}
