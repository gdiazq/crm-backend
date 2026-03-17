package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.QualityOfWorkRequest;
import com.crm.mcsv_rrhh.dto.QualityOfWorkResponse;
import com.crm.mcsv_rrhh.dto.UpdateQualityOfWorkRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface QualityOfWorkService {

    QualityOfWorkResponse create(QualityOfWorkRequest request);

    QualityOfWorkResponse update(UpdateQualityOfWorkRequest request);

    void updateStatus(Long id, Boolean active);

    QualityOfWorkResponse getById(Long id);

    PagedResponse<QualityOfWorkResponse> list(String search, Boolean active,
                                               LocalDate createdFrom, LocalDate createdTo,
                                               LocalDate updatedFrom, LocalDate updatedTo,
                                               Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
