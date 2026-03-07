package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.ContractService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final HRRequestService hrRequestService;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final SafetyGroupRepository safetyGroupRepository;
    private final CompanyRepository companyRepository;
    private final ZoneRepository zoneRepository;
    private final JobTitleRepository jobTitleRepository;
    private final SiteRepository siteRepository;
    private final LaborUnionRepository laborUnionRepository;
    private final MealTypeRepository mealTypeRepository;
    private final TransportTypeRepository transportTypeRepository;

    @Override
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request) {
        Long pendingStatusId = employeeStatusRepository.findByName("Pendiente de revisión")
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: Pendiente de revisión"));

        Contract contract = Contract.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .contractNumber(request.getContractNumber())
                .contractTypeId(request.getContractTypeId())
                .contractStatusId(request.getContractStatusId())
                .safetyGroupId(request.getSafetyGroupId())
                .contractDetail(request.getContractDetail())
                .baseSalary(request.getBaseSalary())
                .agreedSalary(request.getAgreedSalary())
                .companyId(request.getCompanyId())
                .zoneId(request.getZoneId())
                .jobTitleId(request.getJobTitleId())
                .siteId(request.getSiteId())
                .laborUnionId(request.getLaborUnionId())
                .weeklyWorkHours(request.getWeeklyWorkHours())
                .workDays(request.getWorkDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .vacationStartDate(request.getVacationStartDate())
                .mealTypeId(request.getMealTypeId())
                .transportTypeId(request.getTransportTypeId())
                .statusId(pendingStatusId)
                .active(true)
                .build();

        Contract saved = contractRepository.save(contract);
        hrRequestService.createForContract(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        return toDetailResponse(saved);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ContractDetailResponse toDetailResponse(Contract c) {
        return ContractDetailResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolve(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolve(c.getContractStatusId(), contractStatusRepository))
                .safetyGroup(resolve(c.getSafetyGroupId(), safetyGroupRepository))
                .contractDetail(c.getContractDetail())
                .baseSalary(c.getBaseSalary())
                .agreedSalary(c.getAgreedSalary())
                .company(resolve(c.getCompanyId(), companyRepository))
                .zone(resolve(c.getZoneId(), zoneRepository))
                .jobTitle(resolve(c.getJobTitleId(), jobTitleRepository))
                .site(resolve(c.getSiteId(), siteRepository))
                .laborUnion(resolve(c.getLaborUnionId(), laborUnionRepository))
                .weeklyWorkHours(c.getWeeklyWorkHours())
                .workDays(c.getWorkDays())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .vacationStartDate(c.getVacationStartDate())
                .mealType(resolve(c.getMealTypeId(), mealTypeRepository))
                .transportType(resolve(c.getTransportTypeId(), transportTypeRepository))
                .status(resolve(c.getStatusId(), employeeStatusRepository))
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private <T> CatalogItem resolve(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id)
                .map(e -> {
                    try {
                        var getName = e.getClass().getMethod("getName");
                        return new CatalogItem(id, (String) getName.invoke(e));
                    } catch (Exception ex) {
                        return new CatalogItem(id, null);
                    }
                })
                .orElse(null);
    }
}
