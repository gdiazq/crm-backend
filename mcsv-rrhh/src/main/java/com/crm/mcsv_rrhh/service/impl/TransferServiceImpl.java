package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.TransferRequest;
import com.crm.mcsv_rrhh.dto.TransferResponse;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Transfer;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.TransferRepository;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferRepository repository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final ProjectClient projectClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransferResponse> list(Long employeeId, String status, Pageable pageable) {
        Long statusId = status != null && !status.isBlank()
                ? employeeStatusRepository.findByName(status).map(s -> s.getId()).orElse(null)
                : null;

        Specification<Transfer> spec = buildSpec(employeeId, statusId);
        Page<Transfer> page = repository.findAll(spec, pageable);
        long total = repository.count();
        long approved = resolveApprovedCount();

        return PagedResponse.of(page.map(e -> {
            Optional<HRRequest> hrReq = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(e.getId());
            Long reqId = hrReq.map(HRRequest::getId).orElse(null);
            String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
            return toResponse(e, reqId, statusName);
        }), total, approved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse getById(Long id) {
        Transfer entity = findOrThrow(id);
        Optional<HRRequest> hrReq = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(id);
        Long requestId = hrReq.map(HRRequest::getId).orElse(null);
        String statusName = hrReq.map(r -> resolveStatusName(r.getStatusId())).orElse(null);
        return toResponse(entity, requestId, statusName);
    }

    @Override
    @Transactional
    public TransferResponse create(TransferRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));

        if (employee.getCostCenter() == null) {
            throw new IllegalStateException("El empleado no tiene centro de costo asignado");
        }
        if (employee.getCostCenter().equals(request.getToCostCenter())) {
            throw new IllegalStateException("El centro de costo destino es igual al actual");
        }

        Transfer entity = Transfer.builder()
                .employeeId(request.getEmployeeId())
                .fromCostCenter(employee.getCostCenter())
                .toCostCenter(request.getToCostCenter())
                .effectiveDate(request.getEffectiveDate())
                .reason(request.getReason())
                .documentUrl(request.getDocumentUrl())
                .build();

        Transfer saved = repository.save(entity);
        HRRequest hrReq = hrRequestService.createForTransfer(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(saved, hrReq.getId(), statusName);
    }

    @Override
    @Transactional
    public TransferResponse update(UpdateTransferRequest request) {
        Transfer entity = findOrThrow(request.getId());

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        HRRequest hrReq = hrRequestService.createForTransfer(entity.getId(), entity.getEmployeeId(), "UPDATE", proposedData);
        repository.save(entity);

        String statusName = resolveStatusName(hrReq.getStatusId());
        return toResponse(entity, hrReq.getId(), statusName);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Empleado,RUT,Centro Origen,Centro Destino,Fecha Efectiva,Motivo,Estado,Fecha Creación\n");

        repository.findAll().forEach(e -> {
            String statusName = hrRequestRepository.findTopByTransferIdOrderByCreatedAtDesc(e.getId())
                    .map(r -> resolveStatusName(r.getStatusId())).orElse("");
            String fromName = resolveProjectName(e.getFromCostCenter());
            String toName   = resolveProjectName(e.getToCostCenter());

            csv.append(e.getId()).append(",")
               .append(escape(fullName(e.getEmployee()))).append(",")
               .append(e.getEmployee() != null ? e.getEmployee().getIdentification() : "").append(",")
               .append(escape(fromName)).append(",")
               .append(escape(toName)).append(",")
               .append(e.getEffectiveDate() != null ? e.getEffectiveDate() : "").append(",")
               .append(escape(e.getReason())).append(",")
               .append(escape(statusName)).append(",")
               .append(formatDate(e.getCreatedAt())).append("\n");
        });

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Transfer findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traspaso no encontrado con id: " + id));
    }

    private TransferResponse toResponse(Transfer e, Long requestId, String statusName) {
        Employee emp = e.getEmployee();
        return TransferResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(fullName(emp))
                .employeeIdentification(emp != null ? emp.getIdentification() : null)
                .fromCostCenter(e.getFromCostCenter())
                .fromCostCenterName(resolveProjectName(e.getFromCostCenter()))
                .toCostCenter(e.getToCostCenter())
                .toCostCenterName(resolveProjectName(e.getToCostCenter()))
                .effectiveDate(e.getEffectiveDate())
                .reason(e.getReason())
                .documentUrl(e.getDocumentUrl())
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Specification<Transfer> buildSpec(Long employeeId, Long statusId) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (employeeId != null) predicates.add(cb.equal(root.get("employeeId"), employeeId));
            if (statusId != null) {
                var subquery = query.subquery(Long.class);
                var hrRoot = subquery.from(com.crm.mcsv_rrhh.entity.HRRequest.class);
                subquery.select(hrRoot.get("transferId"))
                        .where(
                            cb.equal(hrRoot.get("statusId"), statusId),
                            cb.isNotNull(hrRoot.get("transferId"))
                        );
                predicates.add(root.get("id").in(subquery));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId).map(s -> s.getName()).orElse(null);
    }

    private long resolveApprovedCount() {
        return employeeStatusRepository.findByName("Aprobado")
                .map(s -> hrRequestRepository.findAll().stream()
                        .filter(r -> r.getTransferId() != null && s.getId().equals(r.getStatusId()))
                        .map(HRRequest::getTransferId)
                        .distinct()
                        .count())
                .orElse(0L);
    }

    private String resolveProjectName(Integer costCenter) {
        if (costCenter == null) return null;
        try {
            ProjectClient.ProjectNameDTO dto = projectClient.getByCostCenter(costCenter);
            return dto != null ? dto.getName() : null;
        } catch (Exception e) {
            log.warn("No se pudo resolver nombre de proyecto para costCenter={}: {}", costCenter, e.getMessage());
            return null;
        }
    }

    private String fullName(Employee emp) {
        if (emp == null) return null;
        return String.join(" ", emp.getFirstName(),
                emp.getPaternalLastName() != null ? emp.getPaternalLastName() : "",
                emp.getMaternalLastName() != null ? emp.getMaternalLastName() : "").trim();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
