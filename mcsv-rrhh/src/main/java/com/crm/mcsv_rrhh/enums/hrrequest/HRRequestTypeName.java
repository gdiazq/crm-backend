package com.crm.mcsv_rrhh.enums.hrrequest;

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

    /** Devuelve el tipo cuyo displayName coincide, o {@code null} si no hay coincidencia. */
    public static HRRequestTypeName fromDisplayName(String displayName) {
        for (HRRequestTypeName type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return null;
    }
}
