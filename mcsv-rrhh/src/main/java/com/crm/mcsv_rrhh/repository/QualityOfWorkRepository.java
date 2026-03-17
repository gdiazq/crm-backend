package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.QualityOfWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface QualityOfWorkRepository extends JpaRepository<QualityOfWork, Long>, JpaSpecificationExecutor<QualityOfWork> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<QualityOfWork> findByActiveTrue();
}
