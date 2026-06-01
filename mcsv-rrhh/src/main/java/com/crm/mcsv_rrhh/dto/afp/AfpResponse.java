package com.crm.mcsv_rrhh.dto.afp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfpResponse {
    private Long id;
    private String name;
}
