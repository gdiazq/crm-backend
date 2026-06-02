package com.crm.mcsv_rrhh.service.familyallowancetier.impl;

import com.crm.mcsv_rrhh.dto.familyallowancetier.FamilyAllowanceTierResponse;
import com.crm.mcsv_rrhh.entity.familyallowancetier.FamilyAllowanceTier;
import com.crm.mcsv_rrhh.repository.familyallowancetier.FamilyAllowanceTierRepository;
import com.crm.mcsv_rrhh.service.familyallowancetier.FamilyAllowanceTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyAllowanceTierServiceImpl implements FamilyAllowanceTierService {

    private final FamilyAllowanceTierRepository repository;

    @Override
    public List<FamilyAllowanceTierResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> FamilyAllowanceTierResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
