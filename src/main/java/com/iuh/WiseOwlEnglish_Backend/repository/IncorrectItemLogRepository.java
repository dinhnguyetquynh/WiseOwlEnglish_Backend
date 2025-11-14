package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.IncorrectItemCountDTO;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.model.IncorrectItemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncorrectItemLogRepository extends JpaRepository<IncorrectItemLog, Long> {
    /**
     * Truy vấn đếm số lỗi còn lại (đã cấn trừ) của user cho 1 lesson
     */
    @Query("SELECT new com.iuh.WiseOwlEnglish_Backend.dto.respone.IncorrectItemCountDTO(i.itemRefId, COUNT(i.id)) " +
            "FROM IncorrectItemLog i " +
            "WHERE i.learnerProfile.id = :learnerId " +
            "  AND i.lesson.id = :lessonId " +
            "  AND i.itemType = :itemType " +
            "GROUP BY i.itemRefId " + // Nhóm theo từng từ vựng/câu
            "ORDER BY COUNT(i.id) DESC") // Sắp xếp theo số lỗi nhiều nhất
    List<IncorrectItemCountDTO> findIncorrectItemCounts(
            @Param("learnerId") Long learnerId,
            @Param("lessonId") Long lessonId,
            @Param("itemType") ItemType itemType
    );

    /**
     * Tìm một bản ghi lỗi cũ nhất để xóa khi làm đúng
     */
    Optional<IncorrectItemLog> findFirstByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefIdOrderByWrongAtAsc(
            Long learnerId,
            Long lessonId,
            ItemType itemType,
            Long itemRefId
    );
}
