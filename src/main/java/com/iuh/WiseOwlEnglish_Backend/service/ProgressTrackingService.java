package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonLockStatusRes;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
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
    private final LessonQueryService lessonQueryService;

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

    /**
     * Lấy trạng thái hoàn thành của từng phần trong bài học để quyết định khóa/mở.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true) // Dùng @Transactional (readOnly) của Spring
    public LessonLockStatusRes getLessonLockStatus(Long learnerProfileId, Long lessonId) {

        // --- 1. Kiểm tra Từ vựng (VOCAB) ---
        long totalVocab = lessonQueryService.getTotalVocab(lessonId);
        long completedVocab = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.VOCAB, ItemStatus.COMPLETED);
        boolean vocabLearned = (totalVocab > 0 && completedVocab >= totalVocab);

        // --- 2. Kiểm tra Game Từ Vựng (VOCAB_GAMES) ---
        long totalVocabGames = lessonQueryService.getTotalVocabGameQuestions(lessonId);
        long completedVocabGames = contentProgressRepo.countCompletedGameQuestionsByTypes(
                learnerProfileId, lessonId, ItemStatus.COMPLETED, GameType.VOCAB_GAMES);
        boolean vocabGamesDone = (totalVocabGames > 0 && completedVocabGames >= totalVocabGames);

        // --- 3. Kiểm tra Câu (SENTENCE) ---
        long totalSentence = lessonQueryService.getTotalSentences(lessonId);
        long completedSentence = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.SENTENCE, ItemStatus.COMPLETED);
        boolean sentenceLearned = (totalSentence > 0 && completedSentence >= totalSentence);

        // --- 4. Kiểm tra Game Câu (SENTENCE_GAMES) ---
        long totalSentenceGames = lessonQueryService.getTotalSentenceGameQuestions(lessonId);
        long completedSentenceGames = contentProgressRepo.countCompletedGameQuestionsByTypes(
                learnerProfileId, lessonId, ItemStatus.COMPLETED, GameType.SENTENCE_GAMES);
        boolean sentenceGamesDone = (totalSentenceGames > 0 && completedSentenceGames >= totalSentenceGames);

        // --- 5. Kiểm tra Test (TEST_QUESTION) ---
        long totalTests = lessonQueryService.getTotalTestQuestion(lessonId);
        long completedTests = contentProgressRepo.countByLearnerProfile_IdAndLesson_IdAndItemTypeAndStatus(
                learnerProfileId, lessonId, ItemType.TEST_QUESTION, ItemStatus.COMPLETED);
        boolean allTestsDone = (totalTests > 0 && completedTests >= totalTests);

        // Ghi chú: Logic (total > 0 && completed >= total)
        // Nếu một phần (ví dụ: Học Câu) không có nội dung (total = 0),
        // thì nó sẽ trả về `false` và không bao giờ mở khóa.
        // Nếu bạn muốn "tự động mở" khi không có nội dung, hãy đổi logic thành:
        // boolean vocabLearned = (totalVocab == 0 || completedVocab >= totalVocab);

        return new LessonLockStatusRes(
                vocabLearned,
                vocabGamesDone,
                sentenceLearned,
                sentenceGamesDone,
                allTestsDone
        );
    }
}
