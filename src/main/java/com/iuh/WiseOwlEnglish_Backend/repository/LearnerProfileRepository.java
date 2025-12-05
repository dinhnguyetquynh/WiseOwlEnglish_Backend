package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    List<LearnerProfile> findByUserAccount_Id(Long userId);
    int countByUserAccount_Id(Long userId);

    @Query(value = "SELECT TO_CHAR(created_at, 'MM/YYYY') as month, COUNT(*) as count " +
            "FROM learner_profiles " +
            "WHERE created_at >= NOW() - INTERVAL '12 months' " +
            "GROUP BY TO_CHAR(created_at, 'MM/YYYY'), DATE_TRUNC('month', created_at) " +
            "ORDER BY DATE_TRUNC('month', created_at) ASC", nativeQuery = true)
    List<Object[]> countNewLearnersByMonth();
}
