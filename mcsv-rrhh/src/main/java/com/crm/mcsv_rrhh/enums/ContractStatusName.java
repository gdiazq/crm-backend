package com.crm.mcsv_rrhh.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ContractStatusName {
    ACTIVE("Activo"),
    EXPIRED("Vencido"),
    TERMINATED("Terminado"),
    SUSPENDED("Suspendido");

    private final String displayName;

    ContractStatusName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Set<String> displayNamesOf(ContractStatusName... values) {
        return Arrays.stream(values)
                .map(ContractStatusName::getDisplayName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
