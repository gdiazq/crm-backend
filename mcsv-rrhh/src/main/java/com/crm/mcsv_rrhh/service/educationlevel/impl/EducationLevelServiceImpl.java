package com.crm.mcsv_rrhh.service.educationlevel.impl;

import com.crm.mcsv_rrhh.dto.educationlevel.EducationLevelResponse;
import com.crm.mcsv_rrhh.entity.educationlevel.EducationLevel;
import com.crm.mcsv_rrhh.repository.educationlevel.EducationLevelRepository;
import com.crm.mcsv_rrhh.service.educationlevel.EducationLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationLevelServiceImpl implements EducationLevelService {

    private final EducationLevelRepository repository;

    @Override
    public List<EducationLevelResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> EducationLevelResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
