package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
//    Optional<Test> findByLessonTest_Id(Long lessonId);
    List<Test> findByLessonTest_Id(Long lessonId);
    long countByLessonTest_Id(Long lessonId);

    // === THÊM MỚI ===
    // Tìm Test theo GradeLevel Id thông qua quan hệ LessonTest
    // Sắp xếp theo thứ tự bài học (lesson order) sau đó đến thứ tự tạo test
//    List<Test> findByLessonTest_GradeLevel_IdOrderByLessonTest_OrderIndexAscCreatedAtAsc(Long gradeLevelId);

    @Query("""
        SELECT t FROM Test t 
        JOIN t.lessonTest l 
        WHERE l.gradeLevel.id = :gradeLevelId 
          AND t.deletedAt IS NULL   
        ORDER BY l.orderIndex ASC, t.createdAt ASC
    """)
    List<Test> findTestsByGradeId(@Param("gradeLevelId") Long gradeLevelId);

}
