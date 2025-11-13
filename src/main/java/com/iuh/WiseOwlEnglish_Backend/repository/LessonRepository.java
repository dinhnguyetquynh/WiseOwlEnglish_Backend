package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByGradeLevel_IdAndActiveTrueOrderByOrderIndexAsc(Long gradeLevelId);
    List<Lesson> findByGradeLevel_IdOrderByOrderIndexAsc(Long gradeLevelId);
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.gradeLevel.orderIndex = :orderIndex")
    long countLessonsByGradeOrderIndex(@Param("orderIndex") int orderIndex);
}
