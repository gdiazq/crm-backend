package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeTypeResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal surchargePercent;
    private Boolean nightShift;
    private Boolean holiday;
    private Boolean active;
}
