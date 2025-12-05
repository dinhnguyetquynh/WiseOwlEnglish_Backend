package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LessonCalculatorService {
    private final ContentProgressRepository contentProgressRepo;
    private final LessonProgressRepository lessonProgressRepo;
    private final VocabularyRepository vocabularyRepo;
    private final SentenceRepository sentenceRepo;
    private final GameQuestionRepository gameQuestionRepo; // ✅ DÙNG REPO NÀY
    private final TestQuestionRepository testQuestionRepo; // ✅ DÙNG REPO NÀY
    private final LearnerProfileRepository learnerProfileRepo;
    private final LessonRepository lessonRepo;
    private final LessonQueryService queryService;

    /**
     * Hàm MỚI: Tính toán tiến độ hiện tại và trả về giá trị double.
     * Không @Async để có thể gọi trực tiếp và lấy kết quả ngay.
     */
    public double calculateCurrentProgress(Long learnerProfileId, Long lessonId) {
        // A. Đếm tổng số mục cần học (Lấy realtime từ QueryService đã bỏ cache)
        long totalVocab = queryService.getTotalVocab(lessonId);
        long totalSentences = queryService.getTotalSentences(lessonId);
        long totalVocabGames = queryService.getTotalVocabGameQuestions(lessonId);
        long totalSentenceGames = queryService.getTotalSentenceGameQuestions(lessonId);
        long totalTestQuestions = queryService.getTotalTestQuestion(lessonId);

        double totalItems = totalVocab + totalSentences + totalVocabGames + totalSentenceGames + totalTestQuestions;

        if (totalItems == 0) {
            return 0.0;
        }

        // B. Đếm tổng số mục đã hoàn thành
        long completedVocab = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.VOCAB, ItemStatus.COMPLETED);
        long completedSentences = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.SENTENCE, ItemStatus.COMPLETED);
        long completedVocabGames = contentProgressRepo.countCompletedGameQuestionsByTypes(
                learnerProfileId, lessonId, ItemStatus.COMPLETED, GameType.VOCAB_GAMES);
        long completedSentenceGames = contentProgressRepo.countCompletedGameQuestionsByTypes(
                learnerProfileId, lessonId, ItemStatus.COMPLETED, GameType.SENTENCE_GAMES);
        long completedTestQuestions = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.TEST_QUESTION, ItemStatus.COMPLETED);

        double completedItems = completedVocab + completedSentences + completedVocabGames + completedSentenceGames + completedTestQuestions;

        // C. Tính toán %
        return (completedItems / totalItems) * 100.0;
    }
    @Async
    @Transactional
    public void recalculateLessonPercentage(Long learnerProfileId, Long lessonId, ItemType lastItemType, Long lastItemRefId) {



        // D. Cập nhật bảng tóm tắt LessonProgress
        double percentage = calculateCurrentProgress(learnerProfileId, lessonId);
        updateLessonProgressRecord(learnerProfileId, lessonId, percentage, lastItemType, lastItemRefId);
    }

    /**
     * Phương thức helper (có thể giữ private vì không có @Transactional)
     */
    private void updateLessonProgressRecord(Long learnerProfileId, Long lessonId, double percentage, ItemType lastItemType, Long lastItemRefId) {
        LessonProgress lessonProgress = lessonProgressRepo
                .findByLearnerProfile_IdAndLesson_Id(learnerProfileId, lessonId)
                .orElse(new LessonProgress()); // Tạo mới nếu đây là lần đầu học

        lessonProgress.setLearnerProfile(learnerProfileRepo.getReferenceById(learnerProfileId));
        lessonProgress.setLesson(lessonRepo.getReferenceById(lessonId));
        lessonProgress.setPercentComplete(Math.min(100.0, percentage)); // Đảm bảo không vượt quá 100
        lessonProgress.setLastItemType(lastItemType);
        lessonProgress.setLastItemRefId(lastItemRefId); // ❗️ Vẫn cần chú ý ép kiểu Long về int ở đây
        lessonProgress.setUpdatedAt(LocalDateTime.now());
        // (Bạn có thể set thêm lastItemIndex nếu cần)

        lessonProgressRepo.save(lessonProgress);
    }
}
