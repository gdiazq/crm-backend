package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkImportResult {
    private int total;
    private int success;
    private int failed;
    private List<RowError> errors;

    @Data
    @AllArgsConstructor
    public static class RowError {
        private int row;
        private String message;
    }
}
