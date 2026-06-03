package com.crm.mcsv_rrhh.dto.identificationtype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationTypeResponse {
    private Long id;
    private String name;
}
