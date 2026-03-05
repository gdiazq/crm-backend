package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HRRequestRepository extends JpaRepository<HRRequest, Long> {
    List<HRRequest> findByIdModule(Long idModule);
    Page<HRRequest> findByIdModule(Long idModule, Pageable pageable);
    Page<HRRequest> findByStatusId(Long statusId, Pageable pageable);
    Page<HRRequest> findByIdModuleAndStatusId(Long idModule, Long statusId, Pageable pageable);
    Optional<HRRequest> findTopByIdModuleOrderByCreatedAtDesc(Long idModule);
    long countByIdModule(Long idModule);
    long countByStatusId(Long statusId);
    long countByIdModuleAndStatusId(Long idModule, Long statusId);
    long countByStatusIdIn(Collection<Long> statusIds);
    long countByIdModuleAndStatusIdIn(Long idModule, Collection<Long> statusIds);
}
