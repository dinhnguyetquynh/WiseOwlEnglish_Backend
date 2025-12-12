package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.PromptType;
import com.iuh.WiseOwlEnglish_Backend.model.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GameQuestionRepository extends JpaRepository<GameQuestion,Long> {
    @Query("""
    SELECT gq FROM GameQuestion gq
    WHERE gq.game.id = :gameId
    AND gq.deletedAt IS NULL
    ORDER BY gq.position ASC
""")
    List<GameQuestion> findByGameId(@Param("gameId") Long gameId);

    // 👇 CẬP NHẬT: Chỉ đếm câu hỏi của Game Active và chưa xoá
    @Query("SELECT COUNT(gq) FROM GameQuestion gq " +
            "JOIN gq.game g " +
            "WHERE gq.game.lesson.id = :lessonId " +
            "AND g.active = true " +             // Game phải đang bật
            "AND g.deletedAt IS NULL " +         // Game chưa bị xoá
            "AND gq.deletedAt IS NULL")          // Câu hỏi chưa bị xoá
    long countByLessonId(@Param("lessonId") Long lessonId);


    long countByGameId(Long gameId);

    // 👇 CẬP NHẬT QUAN TRỌNG CHO LỖI CỦA BẠN:
    @Query("SELECT COUNT(gq) FROM GameQuestion gq " +
            "JOIN gq.game g " +
            "WHERE g.lesson.id = :lessonId " +
            "  AND g.type IN :gameTypes " +
            "  AND g.active = true " +           // <--- BẮT BUỘC: Chỉ đếm game đang Active
            "  AND g.deletedAt IS NULL " +       // <--- BẮT BUỘC: Chỉ đếm game chưa xoá
            "  AND gq.deletedAt IS NULL")        // <--- BẮT BUỘC: Chỉ đếm câu hỏi chưa xoá
    long countByLessonIdAndGameTypes(@Param("lessonId") Long lessonId,
                                     @Param("gameTypes") Collection<GameType> gameTypes);

    List<GameQuestion> findByGameIdOrderByPositionAsc(Long gameId);

    // ✅ MỚI: Chỉ lấy câu hỏi CHƯA BỊ XÓA và sắp xếp theo thứ tự
    List<GameQuestion> findByGameIdAndDeletedAtIsNullOrderByPositionAsc(Long gameId);

    // Kiểm tra xem Vocab/Sentence có đang được dùng làm Prompt (đề bài) không
    boolean existsByPromptTypeAndPromptRefIdAndDeletedAtIsNull(PromptType promptType, Long promptRefId);

    long countByDeletedAtIsNull();
}
