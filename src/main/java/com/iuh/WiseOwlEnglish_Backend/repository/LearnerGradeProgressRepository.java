package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerGradeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}