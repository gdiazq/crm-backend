package com.crm.mcsv_rrhh.service.profession.impl;

import com.crm.mcsv_rrhh.dto.profession.ProfessionResponse;
import com.crm.mcsv_rrhh.entity.profession.Profession;
import com.crm.mcsv_rrhh.repository.profession.ProfessionRepository;
import com.crm.mcsv_rrhh.service.profession.ProfessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionServiceImpl implements ProfessionService {

    private final ProfessionRepository repository;

    @Override
    public List<ProfessionResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> ProfessionResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
