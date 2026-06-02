package com.crm.mcsv_rrhh.dto.transporttype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTypeResponse {
    private Long id;
    private String name;
}
