package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByGradeLevel_IdAndActiveTrueOrderByOrderIndexAsc(Long gradeLevelId);
    List<Lesson> findByGradeLevel_IdOrderByOrderIndexAsc(Long gradeLevelId);

}
