package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.GameOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameOptionRepository extends JpaRepository<GameOption,Long> {
    @Query("""
    SELECT go FROM GameOption go
    WHERE go.gameQuestion.id = :questionId
    ORDER BY go.position ASC
    """)
    List<GameOption> findByGameQuestionId(@Param("questionId") Long questionId);

    // ✅ MỚI: Chỉ lấy option CHƯA BỊ XÓA và sắp xếp
    List<GameOption> findByGameQuestionIdAndDeletedAtIsNullOrderByPositionAsc(Long questionId);
}
