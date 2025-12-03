package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.model.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    @Query("SELECT COUNT(cp) FROM ContentProgress cp " +
            "JOIN GameQuestion gq ON cp.itemType = com.iuh.WiseOwlEnglish_Backend.enums.ItemType.GAME_QUESTION AND cp.itemRefId = gq.id " +
            "JOIN gq.game g ON g.id = gq.game.id " +
            "WHERE cp.learnerProfile.id = :learnerProfileId " +
            "  AND cp.lesson.id = :lessonId " +
            "  AND cp.status = :status " +
            "  AND g.type IN :gameTypes")
    long countCompletedGameQuestionsByTypes(
            @Param("learnerProfileId") Long learnerProfileId,
            @Param("lessonId") Long lessonId,
            @Param("status") ItemStatus status,
            @Param("gameTypes") Collection<GameType> gameTypes
    );

    // Thêm hàm này: Kiểm tra xem có bất kỳ ai đã học item này chưa
    boolean existsByItemTypeAndItemRefId(ItemType itemType, Long itemRefId);
}
