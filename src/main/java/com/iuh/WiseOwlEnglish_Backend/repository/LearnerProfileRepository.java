package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    List<LearnerProfile> findByUserAccount_Id(Long userId);
    int countByUserAccount_Id(Long userId);
}
