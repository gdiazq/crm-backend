package com.crm.mcsv_rrhh.dto.mealtype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealTypeResponse {
    private Long id;
    private String name;
}
