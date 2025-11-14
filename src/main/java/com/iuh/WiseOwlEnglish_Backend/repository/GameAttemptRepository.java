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


    /**
     * Tính tổng điểm thưởng (reward) mà một học viên cụ thể kiếm được
     * từ tất cả các game trong một khối (GradeLevel)
     *
     * @param orderIndex Index của GradeLevel
     * @param learnerId  ID của LearnerProfile
     * @return Tổng điểm thưởng (Long)
     */
    @Query("SELECT COALESCE(SUM(ga.rewardCount), 0) FROM GameAttempt ga " +
            "JOIN ga.game g " +
            "JOIN g.lesson l " +
            "JOIN l.gradeLevel gl " +
            "WHERE gl.orderIndex = :orderIndex AND ga.learnerProfile.id = :learnerId")
    Long sumRewardCountByGradeOrderIndexAndLearner(
            @Param("orderIndex") int orderIndex,
            @Param("learnerId") Long learnerId
    );

}
