package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.enums.ItemStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.model.ContentProgress;
import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProgressTrackingService {
    // Inject tất cả các repository cần thiết
    private final ContentProgressRepository contentProgressRepo;
    private final LessonProgressRepository lessonProgressRepo;
    private final VocabularyRepository vocabularyRepo;
    private final SentenceRepository sentenceRepo;
    private final GameQuestionRepository gameQuestionRepo; // ✅ DÙNG REPO NÀY
    private final TestQuestionRepository testQuestionRepo; // ✅ DÙNG REPO NÀY
    private final LearnerProfileRepository learnerProfileRepo;
    private final LessonRepository lessonRepo;
    private final LessonCalculatorService calculatorService;

    /**
     * Hàm này được gọi khi người dùng hoàn thành một mục.
     * VD: xem xong 1 vocab, nộp 1 câu game, nộp 1 câu test.
     */
    @Transactional
    public void markItemCompleted(Long learnerProfileId, Long lessonId, ItemType itemType, Long itemRefId) {

        // 1. Ghi nhận chi tiết vào ContentProgress
        // (Kiểm tra xem mục này đã được hoàn thành chưa, nếu rồi thì có thể bỏ qua)
        ContentProgress progress = contentProgressRepo
                .findByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefId(
                        learnerProfileId, lessonId, itemType, itemRefId)
                .orElse(new ContentProgress()); // Tạo mới nếu chưa có

        // Chỉ cập nhật nếu trạng thái chưa phải là COMPLETED
        if (progress.getStatus() != ItemStatus.COMPLETED) {
            progress.setLearnerProfile(learnerProfileRepo.getReferenceById(learnerProfileId));
            progress.setLesson(lessonRepo.getReferenceById(lessonId));
            progress.setItemType(itemType);
            progress.setItemRefId(itemRefId);
            progress.setStatus(ItemStatus.COMPLETED); // Đánh dấu là đã hoàn thành
            progress.setUpdatedAt(LocalDateTime.now());
            // (Bạn có thể set thêm itemIndex nếu cần)

            contentProgressRepo.save(progress);
        }

        // 2. Tính toán lại % tổng của Lesson
        // Hàm này sẽ được gọi nhiều lần (cho mỗi câu hỏi) nhưng luôn đảm bảo
        // số % là chính xác tại mọi thời điểm.
        calculatorService.recalculateLessonPercentage(learnerProfileId, lessonId, itemType, itemRefId);
    }
}
