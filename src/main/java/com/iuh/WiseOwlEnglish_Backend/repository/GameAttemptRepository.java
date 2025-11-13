package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.AttemptStatus;
import com.iuh.WiseOwlEnglish_Backend.model.GameAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameAttemptRepository extends JpaRepository<GameAttempt, Long> {
    /**
     * Tìm một lần chơi (attempt) dựa trên người chơi và game.
     */
    Optional<GameAttempt> findByLearnerProfile_IdAndGame_IdAndStatus(
            Long learnerProfileId,
            Long gameId,
            AttemptStatus status
    );

    @Query("SELECT COALESCE(SUM(ga.rewardCount), 0) FROM GameAttempt ga " +
            "JOIN ga.game g " +
            "JOIN g.lesson l " +
            "JOIN l.gradeLevel gl " +
            "WHERE gl.orderIndex = :orderIndex")
    Long sumRewardCountByGradeOrderIndex(@Param("orderIndex") int orderIndex);


}
