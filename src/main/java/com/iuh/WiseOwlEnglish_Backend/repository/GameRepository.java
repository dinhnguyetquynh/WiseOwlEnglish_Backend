package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("""
    SELECT g.id FROM Game g
    WHERE g.type = :type
      AND g.lesson.id = :lessonId
    """)
    Optional<Long> findGameIdByTypeAndLessonId(
            @Param("type") GameType type,
            @Param("lessonId") Long lessonId
    );
    // Phương thức kiểm tra sự tồn tại
    boolean existsByTypeAndLessonId(GameType type, Long lessonId);
    List<Game> findByLesson_Id(Long lessonId);
}
