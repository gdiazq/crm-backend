package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.HRRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HRRequestRepository extends JpaRepository<HRRequest, Long>, JpaSpecificationExecutor<HRRequest> {
    List<HRRequest> findByIdModule(Long idModule);
    Page<HRRequest> findByIdModule(Long idModule, Pageable pageable);
    Page<HRRequest> findByStatusId(Long statusId, Pageable pageable);
    Page<HRRequest> findByIdModuleAndStatusId(Long idModule, Long statusId, Pageable pageable);
    Optional<HRRequest> findTopByIdModuleOrderByCreatedAtDesc(Long idModule);
    Optional<HRRequest> findTopByContractIdOrderByCreatedAtDesc(Long contractId);
    Optional<HRRequest> findTopBySettlementIdOrderByCreatedAtDesc(Long settlementId);
    Optional<HRRequest> findTopByTransferIdOrderByCreatedAtDesc(Long transferId);

    @Query("SELECT COUNT(DISTINCT h.settlementId) FROM HRRequest h " +
           "WHERE h.settlementId IS NOT NULL AND h.statusId = :statusId " +
           "AND h.createdAt = (SELECT MAX(h2.createdAt) FROM HRRequest h2 WHERE h2.settlementId = h.settlementId)")
    long countSettlementsWithLatestStatusId(@Param("statusId") Long statusId);
    long countByIdModule(Long idModule);
    long countByStatusId(Long statusId);
    long countByIdModuleAndStatusId(Long idModule, Long statusId);
    long countByStatusIdIn(Collection<Long> statusIds);
    long countByIdModuleAndStatusIdIn(Long idModule, Collection<Long> statusIds);
}
