package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.NoReHiredCause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NoReHiredCauseRepository extends JpaRepository<NoReHiredCause, Long>, JpaSpecificationExecutor<NoReHiredCause> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<NoReHiredCause> findByActiveTrue();
}
