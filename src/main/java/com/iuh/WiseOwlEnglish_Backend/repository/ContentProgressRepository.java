package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.ItemStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.model.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {
    // Dùng để tìm xem user đã học mục này bao giờ chưa
    Optional<ContentProgress> findByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefId(
            Long learnerProfileId, Long lessonId, ItemType itemType, Long itemRefId
    );

    // Dùng để đếm số mục đã hoàn thành theo từng loại
    long countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
            Long learnerProfileId, Long lessonId, ItemType itemType, ItemStatus status
    );
}
