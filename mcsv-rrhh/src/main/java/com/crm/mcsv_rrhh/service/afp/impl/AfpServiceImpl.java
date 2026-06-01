package com.crm.mcsv_rrhh.service.afp.impl;

import com.crm.mcsv_rrhh.dto.afp.AfpResponse;
import com.crm.mcsv_rrhh.entity.afp.Afp;
import com.crm.mcsv_rrhh.repository.afp.AfpRepository;
import com.crm.mcsv_rrhh.service.afp.AfpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AfpServiceImpl implements AfpService {

    private final AfpRepository repository;

    @Override
    public List<AfpResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> AfpResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
