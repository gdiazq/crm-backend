package com.crm.mcsv_rrhh.mapper.transfer;

import com.crm.mcsv_rrhh.dto.transfer.UpdateTransferRequest;
import com.crm.mcsv_rrhh.entity.transfer.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    /** Aplica los campos no nulos de la actualización aprobada sobre el traspaso existente. */
    public void applyUpdate(Transfer transfer, UpdateTransferRequest proposed) {
        if (proposed.getToCostCenter() != null) transfer.setToCostCenter(proposed.getToCostCenter());
        if (proposed.getEffectiveDate() != null) transfer.setEffectiveDate(proposed.getEffectiveDate());
        if (proposed.getReason() != null) transfer.setReason(proposed.getReason());
    }
}
