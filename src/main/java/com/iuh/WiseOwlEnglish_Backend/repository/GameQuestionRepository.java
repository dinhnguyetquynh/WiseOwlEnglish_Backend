package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
