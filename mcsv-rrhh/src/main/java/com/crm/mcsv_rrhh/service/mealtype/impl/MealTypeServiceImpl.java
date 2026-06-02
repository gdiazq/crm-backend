package com.crm.mcsv_rrhh.service.mealtype.impl;

import com.crm.mcsv_rrhh.dto.mealtype.MealTypeResponse;
import com.crm.mcsv_rrhh.entity.mealtype.MealType;
import com.crm.mcsv_rrhh.repository.mealtype.MealTypeRepository;
import com.crm.mcsv_rrhh.service.mealtype.MealTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealTypeServiceImpl implements MealTypeService {

    private final MealTypeRepository repository;

    @Override
    public List<MealTypeResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> MealTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
