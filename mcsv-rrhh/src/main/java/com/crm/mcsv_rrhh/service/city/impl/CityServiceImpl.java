package com.crm.mcsv_rrhh.service.city.impl;

import com.crm.mcsv_rrhh.dto.city.CityResponse;
import com.crm.mcsv_rrhh.entity.city.City;
import com.crm.mcsv_rrhh.repository.city.CityRepository;
import com.crm.mcsv_rrhh.service.city.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository repository;

    @Override
    public List<CityResponse> select(Long communeId) {
        List<City> cities = communeId != null
                ? repository.findByCommuneId(communeId)
                : repository.findAll();
        return cities.stream()
                .map(c -> CityResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }
}
