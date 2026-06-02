package com.crm.mcsv_rrhh.dto.site;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteResponse {
    private Long id;
    private String name;
}
