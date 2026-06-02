package com.crm.mcsv_rrhh.dto.healthinsurancetariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsuranceTariffResponse {
    private Long id;
    private String name;
}
