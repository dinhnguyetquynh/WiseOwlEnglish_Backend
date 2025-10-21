package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.TestOption;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestOptionRepository extends JpaRepository<TestOption, Long> {

    @Query("SELECT o FROM TestOption o WHERE o.question.id = :questionId ORDER BY o.order ASC")
    List<TestOption> findByQuestionIdOrderByOrder(@Param("questionId") Long questionId);
}
