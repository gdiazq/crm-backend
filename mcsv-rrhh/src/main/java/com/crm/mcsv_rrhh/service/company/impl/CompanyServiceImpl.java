package com.crm.mcsv_rrhh.service.company.impl;

import com.crm.mcsv_rrhh.dto.company.CompanyResponse;
import com.crm.mcsv_rrhh.entity.company.Company;
import com.crm.mcsv_rrhh.repository.company.CompanyRepository;
import com.crm.mcsv_rrhh.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository repository;

    @Override
    public List<CompanyResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> CompanyResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
