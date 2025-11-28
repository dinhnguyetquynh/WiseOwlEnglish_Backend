package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.TestAttemptStatus;
import com.iuh.WiseOwlEnglish_Backend.model.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt,Long> {
    /**
     * Lấy điểm của lần làm bài test GẦN NHẤT (status = FINISHED) cho mỗi bài học
     * của một học viên cụ thể.
     * Sử dụng Native Query (PostgreSQL) với DISTINCT ON để tối ưu.
     *
     * @param learnerId ID của LearnerProfile
     * @param lessonIds Danh sách các Lesson ID
     * @return List các mảng Object[], mỗi mảng [0] = lessonId (Long), [1] = score (Double)
     */
    @Query(value = """
        SELECT DISTINCT ON (t.lesson_id) t.lesson_id as lessonId, ta.score
        FROM test_attempt ta
        JOIN test t ON ta.test_id = t.id
        WHERE ta.learner_id = :learnerId
          AND t.lesson_id IN :lessonIds
          AND ta.status = 'FINISHED'
        ORDER BY t.lesson_id, ta.finished_at DESC
    """, nativeQuery = true)
    List<Object[]> findLatestTestScoresNative(
            @Param("learnerId") Long learnerId,
            @Param("lessonIds") List<Long> lessonIds
    );
    /**
     * Tìm tất cả các lần làm bài (đã hoàn thành) của 1 user cho 1 bài test,
     * sắp xếp theo thời gian hoàn thành tăng dần (để vẽ biểu đồ).
     */
    List<TestAttempt> findByLearnerProfile_IdAndTest_IdAndStatusOrderByFinishedAtAsc(
            Long learnerProfileId,
            Long testId,
            TestAttemptStatus status
    );

    // 👇 THÊM MỚI: Kiểm tra xem có lượt làm bài nào cho testId không
    boolean existsByTest_Id(Long testId);

}
