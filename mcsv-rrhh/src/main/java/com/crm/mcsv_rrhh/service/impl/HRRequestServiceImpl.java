package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.dto.ApproveHRRequestRequest;
import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.HRRequestType;
import com.crm.mcsv_rrhh.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.HRRequestTypeRepository;
import com.crm.mcsv_rrhh.service.HRRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRRequestServiceImpl implements HRRequestService {

    private final HRRequestRepository hrRequestRepository;
    private final HRRequestTypeRepository hrRequestTypeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public HRRequest createForEmployee(Long employeeId, String requestTypeName) {
        HRRequestType type = hrRequestTypeRepository.findByName(requestTypeName)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado: " + requestTypeName));

        String initialStatusName = Boolean.TRUE.equals(type.getRequireApproval())
                ? "Pendiente de aprobación"
                : "Pendiente de revisión";

        Long statusId = employeeStatusRepository.findByName(initialStatusName)
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + initialStatusName));

        HRRequest request = HRRequest.builder()
                .requestTypeId(type.getId())
                .statusId(statusId)
                .requireApproval(type.getRequireApproval())
                .idModule(employeeId)
                .build();

        return hrRequestRepository.save(request);
    }

    @Override
    public Page<HRRequestResponse> list(Long idModule, Pageable pageable) {
        if (idModule != null) {
            return hrRequestRepository.findByIdModule(idModule, pageable).map(this::toResponse);
        }
        return hrRequestRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public HRRequestResponse getById(Long id) {
        HRRequest hr = findOrThrow(id);
        return toResponse(hr);
    }

    @Override
    @Transactional
    public HRRequestResponse approve(Long id, ApproveHRRequestRequest req) {
        HRRequest hr = findOrThrow(id);
        String currentStatus = resolveStatusName(hr.getStatusId());

        if ("Pendiente de aprobación".equals(currentStatus)) {
            hr.setApproverId(req.getApproverId());
            hr.setApprovalDate(LocalDateTime.now());
            Long approvedStatusId = resolveStatusId("Aprobado");
            hr.setStatusId(approvedStatusId);
            hrRequestRepository.save(hr);

            Long pendingReviewStatusId = resolveStatusId("Pendiente de revisión");
            HRRequest next = HRRequest.builder()
                    .requestTypeId(hr.getRequestTypeId())
                    .statusId(pendingReviewStatusId)
                    .requireApproval(hr.getRequireApproval())
                    .idModule(hr.getIdModule())
                    .build();
            HRRequest saved = hrRequestRepository.save(next);
            return toResponse(saved);

        } else if ("Pendiente de revisión".equals(currentStatus)) {
            hr.setHhrrApproverId(req.getApproverId());
            hr.setHhrrApprovalDate(LocalDateTime.now());
            Long approvedStatusId = resolveStatusId("Aprobado");
            hr.setStatusId(approvedStatusId);
            hrRequestRepository.save(hr);

            Employee employee = employeeRepository.findById(hr.getIdModule())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));
            employee.setStatusId(approvedStatusId);
            employeeRepository.save(employee);

            return toResponse(hr);

        } else {
            throw new IllegalStateException("La solicitud no está en un estado aprobable");
        }
    }

    @Override
    @Transactional
    public HRRequestResponse reject(Long id, RejectHRRequestRequest req) {
        HRRequest hr = findOrThrow(id);
        Long rejectedStatusId = resolveStatusId("Rechazado");

        hr.setRejectionDetail(req.getRejectionDetail());
        hr.setStatusId(rejectedStatusId);
        hrRequestRepository.save(hr);

        Employee employee = employeeRepository.findById(hr.getIdModule())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con id: " + hr.getIdModule()));
        employee.setStatusId(rejectedStatusId);
        employeeRepository.save(employee);

        return toResponse(hr);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private HRRequest findOrThrow(Long id) {
        return hrRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id: " + id));
    }

    private String resolveStatusName(Long statusId) {
        if (statusId == null) return null;
        return employeeStatusRepository.findById(statusId)
                .map(s -> s.getName())
                .orElse(null);
    }

    private Long resolveStatusId(String name) {
        return employeeStatusRepository.findByName(name)
                .map(s -> s.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + name));
    }

    private HRRequestResponse toResponse(HRRequest hr) {
        String typeName = hrRequestTypeRepository.findById(hr.getRequestTypeId())
                .map(t -> t.getName())
                .orElse(null);
        String statusName = resolveStatusName(hr.getStatusId());

        HRRequestResponse.HRRequestResponseBuilder builder = HRRequestResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestTypeId(hr.getRequestTypeId())
                .requestTypeName(typeName)
                .statusId(hr.getStatusId())
                .statusName(statusName)
                .requireApproval(hr.getRequireApproval())
                .approverId(hr.getApproverId())
                .approvalDate(hr.getApprovalDate())
                .hhrrApproverId(hr.getHhrrApproverId())
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        employeeRepository.findById(hr.getIdModule()).ifPresent(e -> builder
                .identification(e.getIdentification())
                .firstName(e.getFirstName())
                .paternalLastName(e.getPaternalLastName())
                .maternalLastName(e.getMaternalLastName()));

        return builder.build();
    }
}
