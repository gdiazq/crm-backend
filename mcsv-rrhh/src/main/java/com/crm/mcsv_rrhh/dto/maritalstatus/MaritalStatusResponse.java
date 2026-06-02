package com.crm.mcsv_rrhh.dto.maritalstatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaritalStatusResponse {
    private Long id;
    private String name;
}
