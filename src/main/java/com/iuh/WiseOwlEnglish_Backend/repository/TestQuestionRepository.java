package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    @Query("SELECT tq FROM TestQuestion tq WHERE tq.test.id = :testId ORDER BY tq.orderInTest ASC")
    List<TestQuestion> findByTestIdOrderByOrderInTest(@Param("testId") Long testId);
}
