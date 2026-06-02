package com.crm.mcsv_rrhh.service.zone.impl;

import com.crm.mcsv_rrhh.dto.zone.ZoneResponse;
import com.crm.mcsv_rrhh.entity.zone.Zone;
import com.crm.mcsv_rrhh.repository.zone.ZoneRepository;
import com.crm.mcsv_rrhh.service.zone.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository repository;

    @Override
    public List<ZoneResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> ZoneResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
