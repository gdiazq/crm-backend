package com.crm.mcsv_rrhh.service.settlement;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.settlement.QualityOfWorkRequest;
import com.crm.mcsv_rrhh.dto.settlement.QualityOfWorkResponse;
import com.crm.mcsv_rrhh.dto.settlement.QualityOfWorkSelectResponse;
import com.crm.mcsv_rrhh.dto.settlement.UpdateQualityOfWorkRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface QualityOfWorkService {

    QualityOfWorkResponse create(QualityOfWorkRequest request);

    QualityOfWorkResponse update(UpdateQualityOfWorkRequest request);

    void updateStatus(Long id, Boolean active);

    QualityOfWorkResponse getById(Long id);

    List<QualityOfWorkSelectResponse> selectActive();

    PagedResponse<QualityOfWorkResponse> list(String search, Boolean active,
                                               LocalDate createdFrom, LocalDate createdTo,
                                               LocalDate updatedFrom, LocalDate updatedTo,
                                               Pageable pageable);

    byte[] exportCsv();

    BulkImportResult importFromCsv(MultipartFile file);
}
