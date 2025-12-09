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

    List<Lesson> findByGradeLevel_OrderIndex(int orderIndex);

    // 1. Cho Learner: Lấy lesson đang Active và Chưa xoá
    List<Lesson> findAllByGradeLevel_IdAndActiveTrueAndDeletedAtIsNullOrderByOrderIndexAsc(Long gradeLevelId);

    // 2. Cho Admin: Lấy tất cả lesson (kể cả chưa active) nhưng Chưa xoá
    List<Lesson> findByGradeLevel_IdAndDeletedAtIsNullOrderByOrderIndexAsc(Long gradeLevelId);

    // Tìm index lớn nhất dựa trên gradeLevelId. Nếu chưa có bài nào thì trả về 0.
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.gradeLevel.id = :gradeLevelId")
    Integer findMaxOrderIndexByGradeLevelId(@Param("gradeLevelId") Long gradeLevelId);

    long countByDeletedAtIsNull();
}
