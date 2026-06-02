package com.crm.mcsv_rrhh.service.nationality.impl;

import com.crm.mcsv_rrhh.dto.nationality.NationalityResponse;
import com.crm.mcsv_rrhh.entity.nationality.Nationality;
import com.crm.mcsv_rrhh.repository.nationality.NationalityRepository;
import com.crm.mcsv_rrhh.service.nationality.NationalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NationalityServiceImpl implements NationalityService {

    private final NationalityRepository repository;

    @Override
    public List<NationalityResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> NationalityResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
