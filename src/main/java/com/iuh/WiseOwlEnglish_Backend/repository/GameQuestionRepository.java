package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
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
    ORDER BY gq.position ASC
    """)
    List<GameQuestion> findByGameId(@Param("gameId") Long gameId);
    @Query("SELECT COUNT(gq) FROM GameQuestion gq WHERE gq.game.lesson.id = :lessonId")
    long countByLessonId(@Param("lessonId") Long lessonId);
    long countByGameId(Long gameId);

    @Query("SELECT COUNT(gq) FROM GameQuestion gq " +
            "JOIN gq.game g " +
            "WHERE g.lesson.id = :lessonId AND g.type IN :gameTypes")
    long countByLessonIdAndGameTypes(@Param("lessonId") Long lessonId,
                                     @Param("gameTypes") Collection<GameType> gameTypes);

    List<GameQuestion> findByGameIdOrderByPositionAsc(Long gameId);

    // ✅ MỚI: Chỉ lấy câu hỏi CHƯA BỊ XÓA và sắp xếp theo thứ tự
    List<GameQuestion> findByGameIdAndDeletedAtIsNullOrderByPositionAsc(Long gameId);
}
