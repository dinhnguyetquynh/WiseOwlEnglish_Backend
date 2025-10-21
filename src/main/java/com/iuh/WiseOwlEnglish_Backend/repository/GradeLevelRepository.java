package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeLevelRepository extends JpaRepository<GradeLevel, Long> {
    Optional<GradeLevel> findByOrderIndex(int orderIndex);
    List<GradeLevel> findAllByOrderByOrderIndexAsc();
}
