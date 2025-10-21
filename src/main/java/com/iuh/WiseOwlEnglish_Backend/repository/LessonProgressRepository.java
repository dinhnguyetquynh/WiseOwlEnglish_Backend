package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByLearnerProfile_IdAndLesson_Id(Long learnerProfileId, Long lessonId);
    List<LessonProgress> findByLearnerProfile_IdAndLesson_IdIn(Long learnerProfileId, List<Long> lessonIds);
}
