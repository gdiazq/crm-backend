package com.crm.mcsv_rrhh.repository.settlement;

import com.crm.mcsv_rrhh.entity.settlement.TerminationQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface TerminationQuizQuestionRepository extends JpaRepository<TerminationQuizQuestion, Long>,
        JpaSpecificationExecutor<TerminationQuizQuestion> {

    boolean existsByQuestion(String question);

    boolean existsByQuestionAndIdNot(String question, Long id);

    List<TerminationQuizQuestion> findByActiveTrueOrderByCreatedAtDesc();

    List<TerminationQuizQuestion> findByActiveTrueAndEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
