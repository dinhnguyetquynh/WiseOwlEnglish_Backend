package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByLearnerProfile_IdAndLesson_Id(Long learnerProfileId, Long lessonId);
    List<LessonProgress> findByLearnerProfile_IdAndLesson_IdIn(Long learnerProfileId, List<Long> lessonIds);
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.lesson.gradeLevel.orderIndex = :orderIndex")
    List<LessonProgress> findByGradeOrderIndex(@Param("orderIndex") int orderIndex);

    // Kiểm tra xem có bất kỳ tiến độ nào liên quan đến lessonId không
    boolean existsByLesson_Id(Long lessonId);

    //Tìm tất cả bản ghi tiến độ của một bài học cụ thể
    List<LessonProgress> findByLesson_Id(Long lessonId);
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.lesson.id = :lessonId AND lp.percentComplete >= 100")
    long countCompletedByLessonId(@Param("lessonId") Long lessonId);
}
