package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt,Long> {
}
