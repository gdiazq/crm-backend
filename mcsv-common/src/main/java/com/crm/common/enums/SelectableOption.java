package com.crm.common.enums;

/** Opción de selector con id, valor y etiqueta; la implementan los enums de selección. */
public interface SelectableOption {

    Long getId();

    Boolean getValue();

    String getDisplayName();
}
