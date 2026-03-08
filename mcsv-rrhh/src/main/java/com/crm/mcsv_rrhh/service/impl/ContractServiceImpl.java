package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.UpdateContractRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.repository.ContractSpecification;
import com.crm.mcsv_rrhh.service.ContractService;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ObjectMapper objectMapper;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ContractTypeRepository contractTypeRepository;
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
        HRRequest req = hrRequestService.createForContract(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        return toDetailResponse(saved, req.getId());
    }

    // ─── Detalle ──────────────────────────────────────────────────────────────

    @Override
    public ContractDetailResponse getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));
        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(r -> r.getId()).orElse(null);
        return toDetailResponse(contract, requestId);
    }

    // ─── Editar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContractDetailResponse updateContract(Long id, UpdateContractRequest req) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForContract(id, contract.getEmployeeId(), "UPDATE", proposedData);

        Long requestId = hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(id)
                .map(r -> r.getId()).orElse(hrReq.getId());

        return toDetailResponse(contract, requestId);
    }

    // ─── Listar ───────────────────────────────────────────────────────────────

    @Override
    public Page<ContractResponse> list(Long employeeId, Long statusId,
                                       LocalDate createdFrom, LocalDate createdTo,
                                       Pageable pageable) {
        Specification<Contract> spec = ContractSpecification.withFilters(employeeId, statusId, createdFrom, createdTo);
        return contractRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public Map<String, Long> getStats(Long employeeId) {
        Long pendingStatusId = contractStatusRepository.findByName("Pendiente de revisión")
                .map(s -> s.getId()).orElse(-1L);

        long total   = employeeId != null ? contractRepository.countByEmployeeId(employeeId)            : contractRepository.count();
        long active  = employeeId != null ? contractRepository.countByEmployeeIdAndActiveTrue(employeeId) : contractRepository.countByActiveTrue();
        long pending = employeeId != null ? contractRepository.countByEmployeeIdAndStatusId(employeeId, pendingStatusId) : contractRepository.countByStatusId(pendingStatusId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("pending", pending);
        return stats;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ContractResponse toResponse(Contract c) {
        String employeeName = employeeRepository.findById(c.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getPaternalLastName())
                .orElse(null);

        return ContractResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(resolveName(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(resolveName(c.getContractStatusId(), contractStatusRepository))
                .company(resolveName(c.getCompanyId(), companyRepository))
                .jobTitle(resolveName(c.getJobTitleId(), jobTitleRepository))
                .baseSalary(c.getBaseSalary())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ContractDetailResponse toDetailResponse(Contract c, Long requestId) {
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
                .requestId(requestId)
                .build();
    }

    private <T> String resolveName(Long id, JpaRepository<T, Long> repo) {
        if (id == null) return null;
        return repo.findById(id).map(e -> {
            try { return (String) e.getClass().getMethod("getName").invoke(e); }
            catch (Exception ex) { return null; }
        }).orElse(null);
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
