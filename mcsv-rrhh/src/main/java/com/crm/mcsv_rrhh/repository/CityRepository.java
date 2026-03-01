package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndCommuneId(String name, Long communeId);
    List<City> findByCommuneId(Long communeId);
}
