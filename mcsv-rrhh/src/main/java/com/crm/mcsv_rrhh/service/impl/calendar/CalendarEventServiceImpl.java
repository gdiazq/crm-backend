package com.crm.mcsv_rrhh.service.impl.calendar;

import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.calendar.CalendarEventResponse;
import com.crm.mcsv_rrhh.dto.calendar.CalendarEventsResponse;
import com.crm.mcsv_rrhh.service.calendar.CalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.contract.ContractStatus;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import com.crm.mcsv_rrhh.entity.projectassignment.ProjectAssignment;
import com.crm.mcsv_rrhh.repository.contractannex.ContractAnnexRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.leave.EmployeeLeaveRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.hrrequest.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.projectassignment.ProjectAssignmentRepository;
import com.crm.mcsv_rrhh.repository.settlement.SettlementRepository;
import com.crm.mcsv_rrhh.repository.transfer.TransferRepository;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {

    private static final Set<String> SUPPORTED_MODULES = Set.of(
            "LEAVE", "CONTRACT", "ANNEX", "TRANSFER", "SETTLEMENT", "PROJECT"
    );

    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final ContractRepository contractRepository;
    private final ContractAnnexRepository contractAnnexRepository;
    private final TransferRepository transferRepository;
    private final SettlementRepository settlementRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final HRRequestRepository hrRequestRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ProjectClient projectClient;

    @Override
    @Transactional(readOnly = true)
    public CalendarEventsResponse findEvents(LocalDate from,
                                             LocalDate to,
                                             String module,
                                             Long employeeId,
                                             Integer costCenter,
                                             String status) {
        validateRange(from, to);
        String requestedModule = normalizeModule(module);
        List<CalendarEventResponse> events = new ArrayList<>();
        Map<Integer, String> projectNameCache = new HashMap<>();

        if (includes(requestedModule, "LEAVE")) {
            addLeaveEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }
        if (includes(requestedModule, "CONTRACT")) {
            addContractEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }
        if (includes(requestedModule, "ANNEX")) {
            addAnnexEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }
        if (includes(requestedModule, "TRANSFER")) {
            addTransferEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }
        if (includes(requestedModule, "SETTLEMENT")) {
            addSettlementEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }
        if (includes(requestedModule, "PROJECT")) {
            addProjectEvents(events, projectNameCache, from, to, employeeId, costCenter, status);
        }

        events.sort(Comparator
                .comparing(CalendarEventResponse::getDate)
                .thenComparing(CalendarEventResponse::getModule)
                .thenComparing(CalendarEventResponse::getId));

        return CalendarEventsResponse.builder()
                .from(from)
                .to(to)
                .content(events)
                .build();
    }

    private void addLeaveEvents(List<CalendarEventResponse> events,
                                Map<Integer, String> projectNameCache,
                                LocalDate from,
                                LocalDate to,
                                Long employeeId,
                                Integer costCenter,
                                String status) {
        employeeLeaveRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(to, from).forEach(leave -> {
            Employee employee = leave.getEmployee();
            Integer eventCostCenter = contractCostCenter(leave.getContractId());
            String eventStatus = leave.getCurrentStatusName();
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            LocalDate cursor = max(leave.getStartDate(), from);
            LocalDate last = min(leave.getEndDate(), to);
            while (!cursor.isAfter(last)) {
                String leaveTypeName = leave.getLeaveType() != null ? leave.getLeaveType().getName() : "Permiso";
                add(events, CalendarEventResponse.builder()
                        .id("LEAVE-" + leave.getId() + "-" + cursor)
                        .date(cursor.toString())
                        .title("Permiso - " + fullName(employee))
                        .description(joinDescription(leaveTypeName, eventStatus))
                        .module("LEAVE")
                        .entityId(leave.getId())
                        .entityType("LEAVE")
                        .status(eventStatus)
                        .employeeId(leave.getEmployeeId())
                        .employeeFullName(fullName(employee))
                        .costCenter(eventCostCenter)
                        .projectName(resolveProjectName(eventCostCenter, projectNameCache))
                        .tone(resolveTone(eventStatus))
                        .build());
                cursor = cursor.plusDays(1);
            }
        });
    }

    private void addContractEvents(List<CalendarEventResponse> events,
                                   Map<Integer, String> projectNameCache,
                                   LocalDate from,
                                   LocalDate to,
                                   Long employeeId,
                                   Integer costCenter,
                                   String status) {
        contractRepository.findCalendarEvents(from, to).forEach(contract -> {
            Employee employee = contract.getEmployee();
            Integer eventCostCenter = contract.getCostCenter();
            String eventStatus = resolveContractStatus(contract);
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            if (isBetween(contract.getStartDate(), from, to)) {
                add(events, contractEvent(contract, employee, eventCostCenter, eventStatus, projectNameCache,
                        contract.getStartDate(), "START", "Inicio contrato"));
            }
            if (isBetween(contract.getEndDate(), from, to)) {
                add(events, contractEvent(contract, employee, eventCostCenter, eventStatus, projectNameCache,
                        contract.getEndDate(), "END", "Término contrato"));
            }
        });
    }

    private CalendarEventResponse contractEvent(Contract contract,
                                                Employee employee,
                                                Integer eventCostCenter,
                                                String eventStatus,
                                                Map<Integer, String> projectNameCache,
                                                LocalDate date,
                                                String suffix,
                                                String titlePrefix) {
        return CalendarEventResponse.builder()
                .id("CONTRACT-" + contract.getId() + "-" + suffix)
                .date(date.toString())
                .title(titlePrefix + " - " + fullName(employee))
                .description(joinDescription(contract.getName(), eventStatus))
                .module("CONTRACT")
                .entityId(contract.getId())
                .entityType("CONTRACT")
                .status(eventStatus)
                .employeeId(contract.getEmployeeId())
                .employeeFullName(fullName(employee))
                .costCenter(eventCostCenter)
                .projectName(resolveProjectName(eventCostCenter, projectNameCache))
                .tone(resolveTone(eventStatus))
                .build();
    }

    private void addAnnexEvents(List<CalendarEventResponse> events,
                                Map<Integer, String> projectNameCache,
                                LocalDate from,
                                LocalDate to,
                                Long employeeId,
                                Integer costCenter,
                                String status) {
        contractAnnexRepository.findByDateBetween(from, to).forEach(annex -> {
            Employee employee = annex.getEmployee();
            Integer eventCostCenter = contractCostCenter(annex.getContractId());
            String eventStatus = annex.getCurrentStatusName();
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            String annexTypeName = annex.getAnnexType() != null ? annex.getAnnexType().getName() : "Anexo";
            add(events, CalendarEventResponse.builder()
                    .id("ANNEX-" + annex.getId())
                    .date(annex.getDate().toString())
                    .title("Anexo - " + fullName(employee))
                    .description(joinDescription(annexTypeName, eventStatus))
                    .module("ANNEX")
                    .entityId(annex.getId())
                    .entityType("ANNEX")
                    .status(eventStatus)
                    .employeeId(annex.getEmployeeId())
                    .employeeFullName(fullName(employee))
                    .costCenter(eventCostCenter)
                    .projectName(resolveProjectName(eventCostCenter, projectNameCache))
                    .tone(resolveTone(eventStatus))
                    .build());
        });
    }

    private void addTransferEvents(List<CalendarEventResponse> events,
                                   Map<Integer, String> projectNameCache,
                                   LocalDate from,
                                   LocalDate to,
                                   Long employeeId,
                                   Integer costCenter,
                                   String status) {
        transferRepository.findByEffectiveDateBetween(from, to).forEach(transfer -> {
            Employee employee = transfer.getEmployee();
            Integer eventCostCenter = transfer.getToCostCenter();
            String eventStatus = transfer.getCurrentStatusName();
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            add(events, CalendarEventResponse.builder()
                    .id("TRANSFER-" + transfer.getId())
                    .date(transfer.getEffectiveDate().toString())
                    .title("Traspaso - " + fullName(employee))
                    .description("CC " + transfer.getFromCostCenter() + " -> " + transfer.getToCostCenter()
                            + (eventStatus != null ? " · " + eventStatus : ""))
                    .module("TRANSFER")
                    .entityId(transfer.getId())
                    .entityType("TRANSFER")
                    .status(eventStatus)
                    .employeeId(transfer.getEmployeeId())
                    .employeeFullName(fullName(employee))
                    .costCenter(eventCostCenter)
                    .projectName(resolveProjectName(eventCostCenter, projectNameCache))
                    .tone(resolveTone(eventStatus))
                    .build());
        });
    }

    private void addSettlementEvents(List<CalendarEventResponse> events,
                                     Map<Integer, String> projectNameCache,
                                     LocalDate from,
                                     LocalDate to,
                                     Long employeeId,
                                     Integer costCenter,
                                     String status) {
        settlementRepository.findByEndDateBetween(from, to).forEach(settlement -> {
            Employee employee = settlement.getEmployee();
            Integer eventCostCenter = contractCostCenter(settlement.getContractId());
            String eventStatus = hrRequestRepository.findTopBySettlementIdOrderByCreatedAtDesc(settlement.getId())
                    .map(hr -> resolveHrStatus(hr.getStatusId()))
                    .orElse(null);
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            String cause = settlement.getLegalTerminationCause() != null ? settlement.getLegalTerminationCause().getName() : "Finiquito";
            add(events, CalendarEventResponse.builder()
                    .id("SETTLEMENT-" + settlement.getId())
                    .date(settlement.getEndDate().toString())
                    .title("Finiquito - " + fullName(employee))
                    .description(joinDescription(cause, eventStatus))
                    .module("SETTLEMENT")
                    .entityId(settlement.getId())
                    .entityType("SETTLEMENT")
                    .status(eventStatus)
                    .employeeId(settlement.getEmployeeId())
                    .employeeFullName(fullName(employee))
                    .costCenter(eventCostCenter)
                    .projectName(resolveProjectName(eventCostCenter, projectNameCache))
                    .tone(resolveTone(eventStatus))
                    .build());
        });
    }

    private void addProjectEvents(List<CalendarEventResponse> events,
                                  Map<Integer, String> projectNameCache,
                                  LocalDate from,
                                  LocalDate to,
                                  Long employeeId,
                                  Integer costCenter,
                                  String status) {
        projectAssignmentRepository.findCalendarEvents(from, to).forEach(assignment -> {
            Employee employee = assignment.getEmployee();
            Integer eventCostCenter = assignment.getCostCenter();
            String eventStatus = Boolean.TRUE.equals(assignment.getActive()) ? "Activo" : "Programado";
            if (!passesFilters(employee, eventCostCenter, eventStatus, employeeId, costCenter, status)) return;

            if (isBetween(assignment.getStartDate(), from, to)) {
                add(events, projectAssignmentEvent(assignment, employee, projectNameCache,
                        assignment.getStartDate(), "START", "Inicio asignación", eventStatus));
            }
            if (isBetween(assignment.getEndDate(), from, to)) {
                add(events, projectAssignmentEvent(assignment, employee, projectNameCache,
                        assignment.getEndDate(), "END", "Cierre asignación", "Informativo"));
            }
        });
    }

    private CalendarEventResponse projectAssignmentEvent(ProjectAssignment assignment,
                                                         Employee employee,
                                                         Map<Integer, String> projectNameCache,
                                                         LocalDate date,
                                                         String suffix,
                                                         String titlePrefix,
                                                         String eventStatus) {
        String projectName = resolveProjectName(assignment.getCostCenter(), projectNameCache);
        return CalendarEventResponse.builder()
                .id("PROJECT-" + assignment.getId() + "-" + suffix)
                .date(date.toString())
                .title(titlePrefix + " - " + fullName(employee))
                .description(joinDescription(projectName, eventStatus))
                .module("PROJECT")
                .entityId(assignment.getId())
                .entityType("PROJECT_ASSIGNMENT")
                .status(eventStatus)
                .employeeId(assignment.getEmployeeId())
                .employeeFullName(fullName(employee))
                .costCenter(assignment.getCostCenter())
                .projectName(projectName)
                .tone(resolveTone(eventStatus))
                .build();
    }

    private void add(List<CalendarEventResponse> events, CalendarEventResponse event) {
        events.add(event);
    }

    private boolean passesFilters(Employee employee,
                                  Integer eventCostCenter,
                                  String eventStatus,
                                  Long employeeId,
                                  Integer costCenter,
                                  String status) {
        if (employeeId != null && (employee == null || !employeeId.equals(employee.getId()))) return false;
        if (costCenter != null && !costCenter.equals(eventCostCenter)) return false;
        return status == null || status.isBlank() || equalsIgnoreCase(status, eventStatus);
    }

    private String resolveContractStatus(Contract contract) {
        return hrRequestRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId())
                .map(hr -> resolveHrStatus(hr.getStatusId()))
                .orElseGet(() -> contract.getContractStatusId() != null
                        ? contractStatusRepository.findById(contract.getContractStatusId()).map(ContractStatus::getName).orElse(null)
                        : null);
    }

    private String resolveHrStatus(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId)
                .map(EmployeeStatus::getName)
                .orElse(null);
    }

    private String normalizeModule(String module) {
        if (module == null || module.isBlank()) return null;
        String normalized = module.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_MODULES.contains(normalized)) {
            throw new IllegalArgumentException("Módulo de calendario inválido: " + module);
        }
        return normalized;
    }

    private boolean includes(String requestedModule, String module) {
        return requestedModule == null || requestedModule.equals(module);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Los parámetros from y to son obligatorios");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("El parámetro to no puede ser anterior a from");
        }
    }

    private Integer contractCostCenter(Long contractId) {
        if (contractId == null) return null;
        return contractRepository.findById(contractId)
                .map(Contract::getCostCenter)
                .orElse(null);
    }

    private String resolveProjectName(Integer costCenter, Map<Integer, String> cache) {
        if (costCenter == null) return null;
        if (cache.containsKey(costCenter)) return cache.get(costCenter);
        try {
            ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
            String name = project != null ? project.getName() : null;
            cache.put(costCenter, name);
            return name;
        } catch (Exception e) {
            cache.put(costCenter, null);
            return null;
        }
    }

    private String resolveTone(String status) {
        if (status == null) return "slate";
        String normalized = status.toLowerCase(Locale.ROOT);
        if (normalized.contains("aprobado") || normalized.contains("activo") || normalized.contains("presente")
                || normalized.contains("licencia") || normalized.contains("justificado")) {
            return "emerald";
        }
        if (normalized.contains("pendiente") || normalized.contains("revisión") || normalized.contains("revision")
                || normalized.contains("atraso")) {
            return "amber";
        }
        if (normalized.contains("rechazado") || normalized.contains("ausente") || normalized.contains("error")) {
            return "rose";
        }
        if (normalized.contains("programado") || normalized.contains("informativo")) {
            return "cyan";
        }
        return "slate";
    }

    private String fullName(Employee employee) {
        if (employee == null) return "Sin empleado";
        return String.join(" ",
                safe(employee.getFirstName()),
                safe(employee.getPaternalLastName()),
                safe(employee.getMaternalLastName())).trim();
    }

    private String joinDescription(String first, String second) {
        if (first == null || first.isBlank()) return second;
        if (second == null || second.isBlank()) return first;
        return first + " · " + second;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean equalsIgnoreCase(String expected, String actual) {
        return actual != null && expected.trim().equalsIgnoreCase(actual.trim());
    }

    private boolean isBetween(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }
}
