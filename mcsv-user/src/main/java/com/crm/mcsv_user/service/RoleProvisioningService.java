package com.crm.mcsv_user.service;

import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.RoleDTO;

/**
 * Creación de roles en su propio bean para que las llamadas desde flujos batch (import CSV)
 * atraviesen el proxy de Spring y respeten {@code @Transactional} por invocación, sin el truco
 * del método privado "internal" para sortear el self-invocation.
 */
public interface RoleProvisioningService {

    RoleDTO createRole(CreateRoleRequest request);
}
