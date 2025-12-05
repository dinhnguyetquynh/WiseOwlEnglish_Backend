package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.GradeDistribution;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.LearnerStatsRes;
import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerGradeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerGradeProgressRepository extends JpaRepository<LearnerGradeProgress, Long> {
    boolean existsByLearnerProfileIdAndGradeLevelId(Long learnerId, Long gradeId);

    @Query("SELECT lgp.gradeLevel.id FROM LearnerGradeProgress lgp " +
            "WHERE lgp.learnerProfile.id = :learnerId " +
            "AND lgp.status = :status " +
            "AND lgp.isPrimary = true")
    Optional<Long> findGradeLevelIdByLearnerAndStatusAndPrimaryTrue(
            @Param("learnerId") Long learnerId,
            @Param("status") ProgressStatus status
    );

    Optional<LearnerGradeProgress> findByLearnerProfile_IdAndGradeLevel_Id(Long learnerProfileId, Long gradeLevelId);

    // 👇 CẬP NHẬT LẠI QUERY NÀY
    @Query("SELECT new com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.GradeDistribution(" +
            " lgp.gradeLevel.gradeName, COUNT(lgp)) " +
            "FROM LearnerGradeProgress lgp " +
            "WHERE lgp.isPrimary = true " +
            "GROUP BY lgp.gradeLevel.gradeName, lgp.gradeLevel.orderIndex " + // 👈 Thêm orderIndex vào đây
            "ORDER BY lgp.gradeLevel.orderIndex ASC")
    List<GradeDistribution> countLearnersByGrade();

    @Query("SELECT COUNT(lgp) FROM LearnerGradeProgress lgp WHERE lgp.gradeLevel.id = :gradeId")
    long countTotalLearnersInGrade(@Param("gradeId") Long gradeId);
}