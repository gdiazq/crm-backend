package com.crm.mcsv_rrhh.service.safetygroup.impl;

import com.crm.mcsv_rrhh.dto.safetygroup.SafetyGroupResponse;
import com.crm.mcsv_rrhh.entity.safetygroup.SafetyGroup;
import com.crm.mcsv_rrhh.repository.safetygroup.SafetyGroupRepository;
import com.crm.mcsv_rrhh.service.safetygroup.SafetyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SafetyGroupServiceImpl implements SafetyGroupService {

    private final SafetyGroupRepository repository;

    @Override
    public List<SafetyGroupResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> SafetyGroupResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
