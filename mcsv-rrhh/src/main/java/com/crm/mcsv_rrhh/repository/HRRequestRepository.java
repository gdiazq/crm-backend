package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HRRequestRepository extends JpaRepository<HRRequest, Long> {
    List<HRRequest> findByIdModule(Long idModule);
    Page<HRRequest> findByIdModule(Long idModule, Pageable pageable);
    Optional<HRRequest> findTopByIdModuleOrderByCreatedAtDesc(Long idModule);
}
