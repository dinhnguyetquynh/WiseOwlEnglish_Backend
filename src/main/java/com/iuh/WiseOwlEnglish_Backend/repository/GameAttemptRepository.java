package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.RankItem;
import com.iuh.WiseOwlEnglish_Backend.enums.AttemptStatus;
import com.iuh.WiseOwlEnglish_Backend.model.GameAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    @Query("SELECT new com.iuh.WiseOwlEnglish_Backend.dto.respone.RankItem(" +
            "    lp.id," +
            "    lp.nickName," +
            "    lp.avatarUrl," +
            "    COALESCE(SUM(ga.rewardCount), 0L)" +
            ") " +
            "FROM GameAttempt ga " +
            "JOIN ga.learnerProfile lp " + // Tham chiếu đến trường 'learnerProfile' trong GameAttempt
            "WHERE ga.rewardCount > 0 " +
            "GROUP BY lp " + //  SỬA Ở ĐÂY: Group by toàn bộ entity 'lp'
            "ORDER BY COALESCE(SUM(ga.rewardCount), 0L) DESC")
    List<RankItem> findGlobalRanking(Pageable pageable);

    /**
     * Lấy điểm của user hiện tại (kể cả khi = 0)
     */
    @Query("SELECT new com.iuh.WiseOwlEnglish_Backend.dto.respone.RankItem(" +
            "    lp.id," +
            "    lp.nickName," +
            "    lp.avatarUrl," +
            "    COALESCE(SUM(ga.rewardCount), 0L)" +
            ") " +
            "FROM LearnerProfile lp " +
            // 👇 SỬA DÒNG NÀY:
            "LEFT JOIN GameAttempt ga ON ga.learnerProfile = lp " +
            "WHERE lp.id = :profileId " +
            "GROUP BY lp")
    Optional<RankItem> findScoreByProfileId(@Param("profileId") Long profileId);

    /**
     * Đếm số người có điểm cao hơn user hiện tại
     */
    @Query("SELECT COUNT(t.profileId) FROM (" +
            "    SELECT ga.learnerProfile.id as profileId, SUM(ga.rewardCount) as totalScore " +
            "    FROM GameAttempt ga " +
            "    WHERE ga.rewardCount > 0 " +
            "    GROUP BY ga.learnerProfile.id " +
            "    HAVING SUM(ga.rewardCount) > :score" +
            ") t")
    long countUsersWithScoreGreaterThan(@Param("score") long score);

    //Kiểm tra xem Game đã có ai chơi chưa
    boolean existsByGame_Id(Long gameId);

}
