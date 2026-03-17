package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.LegalTerminationCause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LegalTerminationCauseRepository extends JpaRepository<LegalTerminationCause, Long>, JpaSpecificationExecutor<LegalTerminationCause> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<LegalTerminationCause> findByActiveTrue();
}
