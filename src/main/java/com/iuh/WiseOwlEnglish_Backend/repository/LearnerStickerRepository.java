package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.LearnerSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnerStickerRepository extends JpaRepository<LearnerSticker, Long> {
    List<LearnerSticker> findByLearnerProfile_Id(Long learnerId);
    boolean existsByLearnerProfile_IdAndSticker_Id(Long learnerId, Long stickerId);
}
