package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.TerminationQuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerminationQuizAnswerRepository extends JpaRepository<TerminationQuizAnswer, Long> {

    List<TerminationQuizAnswer> findBySettlementIdOrderByIdAsc(Long settlementId);
}
