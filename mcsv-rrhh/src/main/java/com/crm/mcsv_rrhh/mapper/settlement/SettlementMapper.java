package com.crm.mcsv_rrhh.mapper.settlement;

import com.crm.mcsv_rrhh.dto.settlement.UpdateSettlementRequest;
import com.crm.mcsv_rrhh.entity.settlement.Settlement;
import com.crm.mcsv_rrhh.repository.settlement.LegalTerminationCauseRepository;
import com.crm.mcsv_rrhh.repository.settlement.NoReHiredCauseRepository;
import com.crm.mcsv_rrhh.repository.settlement.QualityOfWorkRepository;
import com.crm.mcsv_rrhh.repository.settlement.SafetyComplianceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Aplica una actualización aprobada sobre un finiquito. Resuelve aquí las relaciones por id
 * (causa legal, calidad de trabajo, etc.) para que el service no inyecte esos repositorios.
 */
@Component
@RequiredArgsConstructor
public class SettlementMapper {

    private final LegalTerminationCauseRepository legalTerminationCauseRepository;
    private final QualityOfWorkRepository qualityOfWorkRepository;
    private final SafetyComplianceRepository safetyComplianceRepository;
    private final NoReHiredCauseRepository noReHiredCauseRepository;

    public void applyUpdate(Settlement settlement, UpdateSettlementRequest proposed) {
        if (proposed.getEndDate() != null)
            settlement.setEndDate(proposed.getEndDate());
        if (proposed.getLegalTerminationCauseId() != null)
            settlement.setLegalTerminationCause(
                    legalTerminationCauseRepository.findById(proposed.getLegalTerminationCauseId()).orElse(null));
        if (proposed.getQualityOfWorkId() != null)
            settlement.setQualityOfWork(
                    qualityOfWorkRepository.findById(proposed.getQualityOfWorkId()).orElse(null));
        if (proposed.getSafetyComplianceId() != null)
            settlement.setSafetyCompliance(
                    safetyComplianceRepository.findById(proposed.getSafetyComplianceId()).orElse(null));
        if (proposed.getRehireEligible() != null) {
            settlement.setRehireEligible(proposed.getRehireEligible());
            if (!proposed.getRehireEligible() && proposed.getNoReHiredCauseId() != null)
                settlement.setNoReHiredCause(
                        noReHiredCauseRepository.findById(proposed.getNoReHiredCauseId()).orElse(null));
            else if (proposed.getRehireEligible())
                settlement.setNoReHiredCause(null);
        }
        if (proposed.getObservations() != null)
            settlement.setObservations(proposed.getObservations());
    }
}
