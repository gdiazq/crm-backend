package com.crm.mcsv_rrhh.repository.paymentmethod;

import com.crm.mcsv_rrhh.entity.paymentmethod.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByName(String name);
}
