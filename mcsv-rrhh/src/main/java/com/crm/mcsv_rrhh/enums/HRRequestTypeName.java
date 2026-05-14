package com.crm.mcsv_rrhh.enums;

public enum HRRequestTypeName {
    EMPLOYEE("Trabajador"),
    CONTRACT("Contrato"),
    SETTLEMENT("Finiquito"),
    ANNEX("Anexo"),
    TRANSFER("Traspaso"),
    LEAVE("Permiso"),
    OVERTIME("Horas Extras");

    private final String displayName;

    HRRequestTypeName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
