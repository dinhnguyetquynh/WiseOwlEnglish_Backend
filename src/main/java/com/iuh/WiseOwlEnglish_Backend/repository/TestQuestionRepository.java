package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.StemType;
import com.iuh.WiseOwlEnglish_Backend.model.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TestQuestion t " +
            "WHERE t.stemType = :stemType " +
            "AND t.stemRefId = :stemRefId " +
            "AND t.deletedAt IS NULL")
    boolean existsByStemTypeAndStemRefId(@Param("stemType") StemType stemType,
                                         @Param("stemRefId") Long stemRefId);

    @Query("SELECT tq FROM TestQuestion tq WHERE tq.test.id = :testId ORDER BY tq.orderInTest ASC")
    List<TestQuestion> findByTestIdOrderByOrderInTest(@Param("testId") Long testId);

    // 👇 CẬP NHẬT: Chỉ đếm câu hỏi của Test Active và chưa xoá
    @Query("SELECT COUNT(tq) FROM TestQuestion tq " +
            "JOIN tq.test t " +
            "WHERE t.lessonTest.id = :lessonId " +
            "AND t.active = true " +         // Test đang bật
            "AND t.deletedAt IS NULL")       // Test chưa xoá
    long countByLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT COALESCE(MAX(q.orderInTest), 0) FROM TestQuestion q WHERE q.test.id = :testId")
    int findMaxOrderInTestByTestId(@Param("testId") Long testId);

    // Kiểm tra xem Vocab/Sentence có đang được dùng làm Stem (đề bài) không


}
