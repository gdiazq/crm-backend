package com.crm.mcsv_rrhh.dto.familyallowancetier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyAllowanceTierResponse {
    private Long id;
    private String name;
}
