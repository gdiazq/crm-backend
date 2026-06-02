package com.crm.mcsv_rrhh.service.mealtype;

import com.crm.mcsv_rrhh.dto.mealtype.MealTypeResponse;

import java.util.List;

public interface MealTypeService {

    List<MealTypeResponse> selectAll();
}
