package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("""
    SELECT g.id FROM Game g
    WHERE g.type = :type
      AND g.lesson.id = :lessonId
      AND g.active = true
    """)
    Optional<Long> findGameIdByTypeAndLessonId(
            @Param("type") GameType type,
            @Param("lessonId") Long lessonId
    );

    // Phương thức kiểm tra sự tồn tại
    boolean existsByTypeAndLessonId(GameType type, Long lessonId);

    List<Game> findByLesson_IdAndDeletedAtIsNull(Long lessonId);

    long countByLesson_Id(Long lessonId);

    // --- ĐÃ SỬA: Thêm AndDeletedAtIsNull ---
    List<Game> findByLesson_IdInAndDeletedAtIsNull(List<Long> lessonIds);

    List<Game> findByLesson_IdAndTypeInAndActiveTrue(Long lessonId, Collection<GameType> types);

//    @Query("select g.type from Game g where g.lesson.id = :lessonId and g.type in :types")
//    List<GameType> findTypesByLessonIdAndTypeIn(@Param("lessonId") Long lessonId,
//                                                @Param("types") Collection<GameType> types);
// Đã thêm điều kiện "and g.deletedAt IS NULL"
@Query("select g.type from Game g where g.lesson.id = :lessonId and g.type in :types and g.deletedAt IS NULL")
List<GameType> findTypesByLessonIdAndTypeIn(@Param("lessonId") Long lessonId,
                                            @Param("types") Collection<GameType> types);
}
