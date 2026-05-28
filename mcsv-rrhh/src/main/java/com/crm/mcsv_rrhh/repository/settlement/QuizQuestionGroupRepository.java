package com.crm.mcsv_rrhh.repository.settlement;

import com.crm.mcsv_rrhh.entity.settlement.QuizQuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizQuestionGroupRepository extends JpaRepository<QuizQuestionGroup, Long> {

    List<QuizQuestionGroup> findByActiveTrueOrderByNameAsc();

    Optional<QuizQuestionGroup> findByName(String name);
}
