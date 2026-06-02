package com.crm.mcsv_rrhh.service.gender.impl;

import com.crm.mcsv_rrhh.dto.gender.GenderResponse;
import com.crm.mcsv_rrhh.entity.gender.Gender;
import com.crm.mcsv_rrhh.repository.gender.GenderRepository;
import com.crm.mcsv_rrhh.service.gender.GenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenderServiceImpl implements GenderService {

    private final GenderRepository repository;

    @Override
    public List<GenderResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> GenderResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
