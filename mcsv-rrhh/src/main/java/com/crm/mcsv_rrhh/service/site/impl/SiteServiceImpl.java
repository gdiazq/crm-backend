package com.crm.mcsv_rrhh.service.site.impl;

import com.crm.mcsv_rrhh.dto.site.SiteResponse;
import com.crm.mcsv_rrhh.entity.site.Site;
import com.crm.mcsv_rrhh.repository.site.SiteRepository;
import com.crm.mcsv_rrhh.service.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository repository;

    @Override
    public List<SiteResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> SiteResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
