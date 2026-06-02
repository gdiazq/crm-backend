package com.crm.mcsv_rrhh.dto.pensionstatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PensionStatusResponse {
    private Long id;
    private String name;
}
