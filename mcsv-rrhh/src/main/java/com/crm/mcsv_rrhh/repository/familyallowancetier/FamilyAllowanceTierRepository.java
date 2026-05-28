package com.crm.mcsv_rrhh.repository.familyallowancetier;

import com.crm.mcsv_rrhh.entity.familyallowancetier.FamilyAllowanceTier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FamilyAllowanceTierRepository extends JpaRepository<FamilyAllowanceTier, Long> {
    Optional<FamilyAllowanceTier> findByName(String name);
}
