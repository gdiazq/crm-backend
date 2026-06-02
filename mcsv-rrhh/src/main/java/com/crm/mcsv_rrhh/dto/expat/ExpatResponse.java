package com.crm.mcsv_rrhh.dto.expat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpatResponse {
    private Long id;
    private String name;
}
