package com.crm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Error asociado a una fila del CSV en una importación masiva ({@link BulkImportResult}). */
@Data
@AllArgsConstructor
public class RowError {

    /**
     * Número de línea en el archivo: 1 es el header y los datos parten en 2.
     * El valor 0 indica un error al leer el archivo completo.
     */
    private int row;

    /** Motivo del error, listo para mostrar al usuario. */
    private String message;
}
