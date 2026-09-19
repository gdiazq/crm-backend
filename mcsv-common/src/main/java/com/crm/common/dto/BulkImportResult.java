package com.crm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resultado de una importación masiva por CSV ({@code POST /import/csv} de cada módulo).
 * La importación no es transaccional: las filas válidas quedan guardadas aunque otras fallen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResult {

    /** Filas de datos procesadas (sin contar el header ni las líneas en blanco). */
    private int total;

    /** Filas importadas correctamente. */
    private int success;

    /** Cantidad de errores registrados; coincide con el tamaño de {@link #errors}. */
    private int failed;

    /** Detalle de cada error, con la fila del archivo donde ocurrió. */
    private List<RowError> errors;
}
