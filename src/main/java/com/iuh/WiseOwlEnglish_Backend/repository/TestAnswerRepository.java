package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.TestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestAnswerRepository extends JpaRepository<TestAnswer,Long> {
}
