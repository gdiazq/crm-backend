package com.crm.mcsv_rrhh.mapper.contract;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.contract.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.contract.ContractResponse;
import com.crm.mcsv_rrhh.dto.contract.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.contract.UpdateContractRequest;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.mapper.shared.CatalogResolver;
import com.crm.mcsv_rrhh.service.contract.ContractService;
import com.crm.mcsv_rrhh.util.EmployeeNames;
import com.crm.mcsv_rrhh.repository.company.CompanyRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractTypeRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.jobtitle.JobTitleRepository;
import com.crm.mcsv_rrhh.repository.laborunion.LaborUnionRepository;
import com.crm.mcsv_rrhh.repository.mealtype.MealTypeRepository;
import com.crm.mcsv_rrhh.repository.safetygroup.SafetyGroupRepository;
import com.crm.mcsv_rrhh.repository.site.SiteRepository;
import com.crm.mcsv_rrhh.repository.transporttype.TransportTypeRepository;
import com.crm.mcsv_rrhh.repository.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapea entidades Contract a sus DTOs. Concentra aquí la resolución de catálogos por id y el nombre
 * del proyecto (vía Feign) para que el service no inyecte esos repositorios solo para armar respuestas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractMapper {

    private final EmployeeRepository employeeRepository;
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
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ProjectClient projectClient;

    /**
     * Arma un contrato nuevo a partir del request. El service resuelve y pasa los ids ya calculados:
     * tipo de contrato (puede derivarse a "Indefinido"), estado inicial del contrato y estado de la solicitud.
     */
    public Contract toEntity(CreateContractRequest request, Long contractTypeId,
                             Long contractStatusId, Long statusId) {
        return Contract.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .contractNumber(request.getContractNumber())
                .contractTypeId(contractTypeId)
                .contractStatusId(contractStatusId)
                .safetyGroupId(request.getSafetyGroupId())
                .contractDetail(request.getContractDetail())
                .baseSalary(request.getBaseSalary())
                .agreedSalary(request.getAgreedSalary())
                .companyId(request.getCompanyId())
                .zoneId(request.getZoneId())
                .jobTitleId(request.getJobTitleId())
                .siteId(request.getSiteId())
                .laborUnionId(request.getLaborUnionId())
                .costCenter(request.getCostCenter())
                .weeklyWorkHours(request.getWeeklyWorkHours())
                .workDays(request.getWorkDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .mealTypeId(request.getMealTypeId())
                .transportTypeId(request.getTransportTypeId())
                .statusId(statusId)
                .build();
    }

    /** Copia los campos de una solicitud de actualización aprobada sobre el contrato existente. */
    public void applyUpdate(Contract contract, UpdateContractRequest proposed) {
        contract.setName(proposed.getName());
        contract.setContractNumber(proposed.getContractNumber());
        contract.setContractTypeId(proposed.getContractTypeId());
        contract.setSafetyGroupId(proposed.getSafetyGroupId());
        contract.setContractDetail(proposed.getContractDetail());
        contract.setBaseSalary(proposed.getBaseSalary());
        contract.setAgreedSalary(proposed.getAgreedSalary());
        contract.setCompanyId(proposed.getCompanyId());
        contract.setZoneId(proposed.getZoneId());
        contract.setJobTitleId(proposed.getJobTitleId());
        contract.setSiteId(proposed.getSiteId());
        contract.setLaborUnionId(proposed.getLaborUnionId());
        contract.setCostCenter(proposed.getCostCenter());
        contract.setWeeklyWorkHours(proposed.getWeeklyWorkHours());
        contract.setWorkDays(proposed.getWorkDays());
        contract.setStartDate(proposed.getStartDate());
        contract.setEndDate(proposed.getEndDate());
        contract.setMealTypeId(proposed.getMealTypeId());
        contract.setTransportTypeId(proposed.getTransportTypeId());
    }

    /** Resumen de contrato para el listado. */
    public ContractResponse toResponse(Contract c) {
        Employee employee = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFirstName() + " " + employee.getPaternalLastName() : null;
        String employeeIdentification = employee != null ? employee.getIdentification() : null;

        return ContractResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .employeeIdentification(employeeIdentification)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(CatalogResolver.name(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(CatalogResolver.name(c.getContractStatusId(), contractStatusRepository))
                .company(CatalogResolver.name(c.getCompanyId(), companyRepository))
                .jobTitle(CatalogResolver.name(c.getJobTitleId(), jobTitleRepository))
                .costCenter(c.getCostCenter())
                .projectName(resolveProjectName(c.getCostCenter()))
                .baseSalary(c.getBaseSalary())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    /** Detalle de contrato (incluye request asociada y documentos resueltos por el service). */
    public ContractDetailResponse toDetailResponse(Contract c, Long requestId, List<FileMetadataResponse> documents) {
        Employee employee = employeeRepository.findById(c.getEmployeeId()).orElse(null);
        String employeeName = employee != null ? employee.getFirstName() + " " + employee.getPaternalLastName() : null;
        String employeeIdentification = employee != null ? employee.getIdentification() : null;

        return ContractDetailResponse.builder()
                .id(c.getId())
                .employeeId(c.getEmployeeId())
                .employeeName(employeeName)
                .employeeIdentification(employeeIdentification)
                .name(c.getName())
                .contractNumber(c.getContractNumber())
                .contractType(CatalogResolver.item(c.getContractTypeId(), contractTypeRepository))
                .contractStatus(CatalogResolver.item(c.getContractStatusId(), contractStatusRepository))
                .safetyGroup(CatalogResolver.item(c.getSafetyGroupId(), safetyGroupRepository))
                .contractDetail(c.getContractDetail())
                .baseSalary(c.getBaseSalary())
                .agreedSalary(c.getAgreedSalary())
                .company(CatalogResolver.item(c.getCompanyId(), companyRepository))
                .zone(CatalogResolver.item(c.getZoneId(), zoneRepository))
                .jobTitle(CatalogResolver.item(c.getJobTitleId(), jobTitleRepository))
                .site(CatalogResolver.item(c.getSiteId(), siteRepository))
                .laborUnion(CatalogResolver.item(c.getLaborUnionId(), laborUnionRepository))
                .costCenter(c.getCostCenter())
                .projectName(resolveProjectName(c.getCostCenter()))
                .weeklyWorkHours(c.getWeeklyWorkHours())
                .workDays(c.getWorkDays())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .mealType(CatalogResolver.item(c.getMealTypeId(), mealTypeRepository))
                .transportType(CatalogResolver.item(c.getTransportTypeId(), transportTypeRepository))
                .status(CatalogResolver.item(c.getStatusId(), employeeStatusRepository))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .requestId(requestId)
                .documents(documents)
                .build();
    }

    /* Helper mapper */

    /** Item de selección para asistencia: empleado (nombre completo) + centro de costo del contrato. */
    public ContractService.AttendanceEmployeeSelectItem toAttendanceSelectItem(Contract c) {
        Employee e = c.getEmployee();
        return new ContractService.AttendanceEmployeeSelectItem(e.getId(), EmployeeNames.full(e), c.getCostCenter());
    }

    private String resolveProjectName(Integer costCenter) {
        if (costCenter == null) return null;
        try {
            ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
            return project != null ? project.getName() : null;
        } catch (Exception e) {
            log.warn("No se pudo resolver nombre de proyecto para costCenter={}: {}", costCenter, e.getMessage());
            return null;
        }
    }
}
