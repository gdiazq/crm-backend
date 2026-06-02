package com.crm.mcsv_rrhh.dto.retirementstatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetirementStatusResponse {
    private Long id;
    private String name;
}
