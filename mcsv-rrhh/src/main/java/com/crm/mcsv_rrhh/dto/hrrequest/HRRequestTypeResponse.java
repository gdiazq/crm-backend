package com.crm.mcsv_rrhh.dto.hrrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRRequestTypeResponse {
    private Long id;
    private String name;
}
